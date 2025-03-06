package com.swust.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.apis.wmuser.IWmUserClient;
import com.swust.common.utils.SaltGenerator;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.AppHttpCodeEnum;
import com.swust.model.user.dtos.LoginDto;
import com.swust.model.user.dtos.UserDto;
import com.swust.model.user.pojos.ApUser;
import com.swust.model.user.vos.RoleVO;
import com.swust.model.user.vos.UserVO;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.user.mapper.ApUserMapper;
import com.swust.user.mapper.RoleMapper;
import com.swust.user.mapper.UserRoleMapper;
import com.swust.user.service.ApUserService;
import com.swust.utils.common.AppJwtUtil;
import com.swust.utils.common.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Autowired
    UserRoleMapper userRoleMapper;
    @Autowired
    IWmUserClient wmUserClient;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * app端登录功能
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //1.正常登录 用户名和密码
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            //1.1 根据手机号查询用户信息
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户信息不存在");
            }

            //判断用户账号是否启用
            if (dbUser.getStatus()) {
                return ResponseResult.errorResult(AppHttpCodeEnum.ACCOUNT_LOCKED);
            }

            //1.2 比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!pswd.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3 返回数据  jwt  user
            ApUser currentUser = new ApUser();
            currentUser.setId(dbUser.getId());
            currentUser.setImage(dbUser.getImage());
            currentUser.setName(dbUser.getName());
            String token = AppJwtUtil.getToken(currentUser);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user", dbUser);
            //查询该用户角色
            RoleVO role = userRoleMapper.getByUserId(dbUser.getId());
            map.put("role",role);
            //将登录信息存入redis
            stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(currentUser), 3600 * 1000, TimeUnit.MILLISECONDS);
            return ResponseResult.okResult(map);
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
//            //2.游客登录
//            Map<String,Object> map = new HashMap<>();
//            map.put("token",AppJwtUtil.getToken(0L));
//            return ResponseResult.okResult(map);
        }


    }

    @Override
    public List<UserVO> page(UserDto userDto) {
        if (userDto == null) {
            userDto = new UserDto();
        }
        userDto.checkParam();
        IPage<ApUser> iPage = new Page<>(userDto.getPage(), userDto.getSize());
        QueryWrapper<ApUser> wrapper = new QueryWrapper<>();
        wrapper.select(Arrays.asList("id", "name", "phone", "image", "sex", "status", "flag", "created_time"));
        wrapper.like(StringUtils.isNotBlank(userDto.getName()), "name", userDto.getName())
                .like(StringUtils.isNotBlank(userDto.getPhone()), "phone", userDto.getPhone())
                .eq(userDto.getSex() != null, "sex", userDto.getSex());
        List<UserVO> userVOList = list(iPage, wrapper).stream().map(apUser -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(apUser, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }

    @Override
    public UserVO getById(Long id) {
        ApUser user = this.baseMapper.selectById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRole(userRoleMapper.getByUserId(user.getId()));
        return userVO;
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
        boolean saved = this.save(userDto);
        //向自媒体用户表插入数据
        WmUser wmUser = new WmUser();
        wmUser.setApUserId(userDto.getId());
        wmUser.setImage(userDto.getImage());
        wmUser.setSalt(userDto.getSalt());
        wmUser.setPassword(userDto.getPassword());
        wmUser.setName(userDto.getName());
        wmUser.setPhone(userDto.getPhone());
//        wmUser.setNickname(userDto.get);
        ResponseResult addedUser = wmUserClient.addUser(wmUser);
        return saved && addedUser.getCode() == HttpStatus.SC_OK;
    }

    @Override
    public UserVO currentUser(String token) {
        String userStr = stringRedisTemplate.opsForValue().get(token);
        if (userStr == null || Objects.equals(userStr,"")) {
            return null;
        }
        ApUser curUser = JSON.parseObject(userStr, ApUser.class);
        UserVO user = new UserVO();
        List<String> menuIds = userRoleMapper.selectMenuIdsByUserId(curUser.getId());
        user.setId(curUser.getId());
        user.setMenus(menuIds);
        user.setName(curUser.getName());
        user.setRole(userRoleMapper.getByUserId(user.getId()));
        return user;
    }

    @Override
    public ResponseResult logout(String token) {
        Boolean deleted = stringRedisTemplate.delete(token);
        return ResponseResult.okResult(deleted);
    }

    @Override
    public ResponseResult auth(String token) {
        String s = stringRedisTemplate.opsForValue().get(token);
        if (s == null) {
            return ResponseResult.errorResult(HttpStatus.SC_UNAUTHORIZED, "登录信息过期");
        }
        return ResponseResult.okResult(HttpStatus.SC_OK);
    }
}
