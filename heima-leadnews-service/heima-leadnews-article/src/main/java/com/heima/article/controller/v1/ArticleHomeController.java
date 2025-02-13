package com.heima.article.controller.v1;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.service.ArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/article")
@Api(value = "文章列表接口")
public class ArticleHomeController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/list")
    public ResponseResult list(){
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<ApArticle>()
                .eq(ApArticle::getChannelId, 1);
        return ResponseResult.okResult(articleService.list(wrapper));
    }

    //Golang 发起请求时 可以不传递 也能调用shouledBindJson进行参数绑定
    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) ArticleHomeDto dto){
        return ResponseResult.okResult(articleService.page(dto));
    }

    @PostMapping("/load")
    @ApiOperation(value = "加载文章列表")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto){
        articleService.loadArticle(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE);
        return ResponseResult.okResult(articleService.list());
    }

    @PostMapping("/loadmore")
    @ApiOperation("加载更多")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return ResponseResult.okResult(articleService.loadArticle(dto,ArticleConstants.LOADTYPE_LOAD_MORE));
    }

    @PostMapping("/loadnew")
    @ApiOperation("刷新")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return ResponseResult.okResult(articleService.loadArticle(dto,ArticleConstants.LOADTYPE_LOAD_NEW));
    }

    @GetMapping("/type/list")
    @ApiOperation("获取文章频道信息")
    public ResponseResult getArticleTypes() {
        return ResponseResult.okResult(articleService.getArticleTypes());
    }

    @GetMapping("/dep")
    @ApiOperation("获取部门信息")
    public ResponseResult getdep() {
        return ResponseResult.okResult(articleService.getDep());
    }
}
