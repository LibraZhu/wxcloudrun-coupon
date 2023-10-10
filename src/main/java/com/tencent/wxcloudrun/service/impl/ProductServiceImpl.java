package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.ResultCode;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.ProductDetailParam;
import com.tencent.wxcloudrun.dto.ProductLinkParam;
import com.tencent.wxcloudrun.dto.ProductQueryParam;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.service.*;
import com.tencent.wxcloudrun.utils.RequestHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductServiceImpl implements ProductService {
  @Resource JDService jdService;
  @Resource PddService pddService;
  @Resource TBService tbService;
  @Resource WPHService wpService;
  @Resource DYService dyService;

  @Override
  public List<HJKJDProduct> searchLink(ProductQueryParam param) {
    if (param.getKeyword().startsWith("https://item.m.jd.com")
        || param.getKeyword().startsWith("https://item.jd.com")) {
      CommonPage<HJKJDProduct> page = jdService.searchProduct(param);
      if (ObjectUtil.isEmpty(page.getList())) {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
      return page.getList();
    } else if (param.getKeyword().contains("yangkeduo.com")) {
      CommonPage<HJKJDProduct> page = pddService.searchProduct(param);
      if (ObjectUtil.isEmpty(page.getList())) {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
      return page.getList();
    } else if (param.getKeyword().contains("m.tb.cn")) {
      Matcher matcher =
          Pattern.compile(Pattern.quote("「") + "(.*?)" + Pattern.quote("」"))
              .matcher(param.getKeyword());
      if (matcher.find()) {
        String keyword = matcher.group(1).trim();
        param.setKeyword(keyword);
        CommonPage<HJKJDProduct> page = tbService.searchProduct(param);
        if (ObjectUtil.isEmpty(page.getList())) {
          Asserts.fail(ResultCode.NO_PRODUCT);
        }
        return page.getList();
      } else {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
    } else if (param.getKeyword().contains("item.taobao.com")
        || param.getKeyword().contains("detail.tmall.com")) {
      String content = URLDecoder.decode(param.getKeyword(), StandardCharsets.UTF_8);
      String id = content.substring(content.indexOf("id=") + 3).split("&")[0];
      HJKJDProduct product = tbService.getProductDetail(id);
      if (product == null) {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
      return Collections.singletonList(product);
    } else if (param.getKeyword().contains("v.douyin.com")) {
      param.setKeyword(param.getKeyword());
      param.setUid(RequestHolder.getUid());
      CommonPage<HJKJDProduct> page = dyService.searchProduct(param);
      if (ObjectUtil.isEmpty(page.getList())) {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
      Optional<HJKJDProduct> p =
          page.getList().stream()
              .filter(item -> param.getKeyword().contains(item.getGoods_name()))
              .findFirst();
      if (p.isPresent()) {
        List<HJKJDProduct> list = new ArrayList<>();
        list.add(p.get());
        return list;
      }
      return page.getList();
    } else if (param.getKeyword().contains("t.vip.com")) {
      String content = URLDecoder.decode(param.getKeyword(), StandardCharsets.UTF_8);
      String goodsId = content.substring(content.indexOf("goodsId=") + 8).split("&")[0];
      if (ObjectUtil.isEmpty(goodsId)) {
        Asserts.fail(ResultCode.NO_PRODUCT);
      }
      return Collections.singletonList(wpService.getProductDetail(goodsId));
    }
    return null;
  }

  @Override
  public CommonPage<HJKJDProduct> search(ProductQueryParam param) {
    if (ObjectUtil.equals(param.getSource(), ProductSource.PDD.getCode())) {
      return pddService.searchProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.TB.getCode())) {
      return tbService.searchProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.WPH.getCode())) {
      return wpService.searchProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.DY.getCode())) {
      param.setUid(RequestHolder.getUid());
      return dyService.searchProduct(param);
    }
    return jdService.searchProduct(param);
  }

  @Override
  public CommonPage<HJKJDProduct> list(ProductQueryParam param) {
    if (ObjectUtil.equals(param.getSource(), ProductSource.PDD.getCode())) {
      return pddService.searchProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.TB.getCode())) {
      param.setIsTmall(true);
      return tbService.searchProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.WPH.getCode())) {
      return wpService.listProduct(param);
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.DY.getCode())) {
      return dyService.listProduct(param);
    }
    return jdService.listProduct(param);
  }

  @Override
  public HJKJDProduct detail(ProductDetailParam param) {
    if (ObjectUtil.equals(param.getSource(), ProductSource.PDD.getCode())) {
      return pddService.getProductDetail(param.getProductId(), param.getSearchId());
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.TB.getCode())) {
      return tbService.getProductDetail(param.getProductId());
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.WPH.getCode())) {
      return wpService.getProductDetail(param.getProductId());
    }
    if (ObjectUtil.equals(param.getSource(), ProductSource.DY.getCode())) {
      return dyService.getProductDetail(param.getProductId(), RequestHolder.getUid());
    }
    return jdService.getProductDetail(param.getProductId());
  }

  @Override
  public Object link(ProductLinkParam param) {
    if (param.getSource() == ProductSource.PDD.getCode()) {
      return pddService.getUnionUrl(
          param.getProductId(), param.getSearchId(), RequestHolder.getUid());
    }
    if (param.getSource() == ProductSource.TB.getCode()) {
      return tbService.getUnionUrl(param.getProductId(), RequestHolder.getUid());
    }
    if (param.getSource() == ProductSource.WPH.getCode()) {
      return wpService.getUnionUrl(param.getProductId(), RequestHolder.getUid());
    }
    if (param.getSource() == ProductSource.DY.getCode()) {
      return dyService.getUnionUrl(param.getProductId(), RequestHolder.getUid());
    }
    return jdService.getUnionUrl(
        param.getProductId(), param.getCouponUrl(), RequestHolder.getUid());
  }

  @Override
  public Object fenLink(ProductLinkParam param) {
    if (param.getSource() == ProductSource.DY.getCode()) {
      return dyService.getUnionUrl(param.getProductId(), "F" + RequestHolder.getUid());
    }
    return null;
  }

  @Override
  public CommonPage<HJKJDProduct> hotList(ProductQueryParam param) {
    if (param.getSource() == ProductSource.PDD.getCode()) {
      param.setChannelType(5);
      return pddService.listProduct(param);
    }
    if (param.getSource() == ProductSource.TB.getCode()) {
      param.setOptId(28026L);
      return tbService.listProduct(param);
    }
    if (param.getSource() == ProductSource.WPH.getCode()) {
      param.setJxCode("4hm6c35w");
      return wpService.listProduct(param);
    }
    if (param.getSource() == ProductSource.DY.getCode()) {
      return dyService.rankProduct(param);
    }
    param.setOptId(22L);
    return jdService.listProduct(param);
  }
}
