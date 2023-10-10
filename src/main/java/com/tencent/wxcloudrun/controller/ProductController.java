package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.common.api.ResultCode;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.ProductDetailParam;
import com.tencent.wxcloudrun.dto.ProductLinkParam;
import com.tencent.wxcloudrun.dto.ProductQueryParam;
import com.tencent.wxcloudrun.service.DYService;
import com.tencent.wxcloudrun.service.ProductService;
import com.tencent.wxcloudrun.service.UmsUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 订单表 前端控制器
 *
 * @author zjf
 * @since 2023年07月11日
 */
@RestController
@RequestMapping("/product")
public class ProductController {
  DYService dyService;
  ProductService productService;
  UmsUserService umsUserService;

  public ProductController(
      @Autowired DYService dyService,
      @Autowired ProductService productService,
      @Autowired UmsUserService umsUserService) {
    this.dyService = dyService;
    this.productService = productService;
    this.umsUserService = umsUserService;
  }

  @ApiOperation("通过链接进行商品搜索")
  @PostMapping("/searchLink")
  @ResponseBody
  public CommonResult<List<HJKJDProduct>> searchLink(@RequestBody ProductQueryParam param) {
    return Optional.ofNullable(productService.searchLink(param))
        .map(CommonResult::success)
        .orElseGet(() -> CommonResult.failed(ResultCode.NO_PRODUCT));
  }

  @ApiOperation("商品搜索")
  @PostMapping("/search")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> search(@RequestBody ProductQueryParam param) {
    return CommonResult.success(productService.search(param));
  }

  @ApiOperation("商品列表")
  @PostMapping("/list")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> list(@RequestBody ProductQueryParam param) {
    return CommonResult.success(productService.list(param));
  }

  @ApiOperation("商品详情")
  @PostMapping("/detail")
  @ResponseBody
  public CommonResult<HJKJDProduct> detail(@Validated @RequestBody ProductDetailParam param) {
    return CommonResult.success(productService.detail(param));
  }

  @ApiOperation("商品转链")
  @PostMapping("/link")
  @ResponseBody
  public CommonResult<Object> link(@Validated @RequestBody ProductLinkParam param) {
    return CommonResult.success(productService.link(param));
  }

  @ApiOperation("一分购商品转链")
  @PostMapping("/fenLink")
  @ResponseBody
  public CommonResult<Object> fenLink(@Validated @RequestBody ProductLinkParam param) {
    return CommonResult.success(productService.fenLink(param));
  }

  @ApiOperation("实时热销商品列表")
  @PostMapping("/hotList")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> hotList(@RequestBody ProductQueryParam param) {
    return CommonResult.success(productService.hotList(param));
  }

  @ApiOperation("抖音一分购")
  @PostMapping("/oneFenList")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> oneFenList(@RequestBody ProductQueryParam param) {
    return CommonResult.success(dyService.oneFenProduct(param));
  }

  @ApiOperation("抖音一元购")
  @PostMapping("/oneYuanList")
  @ResponseBody
  public CommonResult<CommonPage<HJKJDProduct>> oneYuanList(@RequestBody ProductQueryParam param) {
    return CommonResult.success(dyService.oneYuanProduct(param));
  }
}
