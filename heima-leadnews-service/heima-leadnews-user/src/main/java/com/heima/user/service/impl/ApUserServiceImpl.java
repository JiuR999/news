package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.utils.SaltGenerator;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.vos.UserVO;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.UserRoleMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Autowired
    UserRoleMapper userRoleMapper;
    /**
     * app端登录功能
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //1.正常登录 用户名和密码
        if(StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            //1.1 根据手机号查询用户信息
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if(dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户信息不存在");
            }

            //1.2 比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(!pswd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3 返回数据  jwt  user
            ApUser currentUser = new ApUser();
            currentUser.setId(dbUser.getId());
            currentUser.setImage(dbUser.getImage());
            currentUser.setName(dbUser.getName());
            String token = AppJwtUtil.getToken(currentUser);
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user",dbUser);
            return ResponseResult.okResult(map);
        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
//            //2.游客登录
//            Map<String,Object> map = new HashMap<>();
//            map.put("token",AppJwtUtil.getToken(0L));
//            return ResponseResult.okResult(map);
        }


    }

    @Override
    public List<UserVO> page(UserDto userDto) {
        if(userDto == null) {
            userDto = new UserDto();
        }
        userDto.checkParam();
        IPage<ApUser> iPage = new Page<>(userDto.getPage(),userDto.getSize());
        QueryWrapper<ApUser> wrapper = new QueryWrapper<>();
        wrapper.select(Arrays.asList("id","name","phone","image","sex","status","flag","created_time"));
        List<UserVO> userVOList = list(iPage, wrapper).stream().map(apUser -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(apUser, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }

    @Override
    public ApUser info() {
        return null;
    }

    @Override
    public void resetPassword() {
        //校验是否是本人

        //重置密码为默认密码123456

        //更新数据库
    }

    @Override
    public Boolean add(ApUser userDto) {
        //生成密码加密盐
        String salt = SaltGenerator.generateSalt(6);
        userDto.setSalt(salt);
        //对密码进行MD5加密
        String mded = DigestUtils.md5DigestAsHex((userDto.getPassword() + salt).getBytes());
        userDto.setPassword(mded);
        return this.save(userDto);
    }

    @Override
    public UserVO currentUser(Integer userId) {
//        Claims claims = AppJwtUtil.getClaimsBody(token);
//        Object o = claims.get(AppJwtUtil.CURRENT_USER);
//        UserVO user = JSON.parseObject((String) o, UserVO.class);
        UserVO user = new UserVO();
        ApUser apUser = getById(userId);
        List<String> menuIds = userRoleMapper.selectMenuIdsByUserId(userId);
        user.setMenus(menuIds);
        user.setName(apUser.getName());
        return user;
    }
}
