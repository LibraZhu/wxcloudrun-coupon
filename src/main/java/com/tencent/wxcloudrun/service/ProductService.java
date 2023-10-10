package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.ProductDetailParam;
import com.tencent.wxcloudrun.dto.ProductLinkParam;
import com.tencent.wxcloudrun.dto.ProductQueryParam;

import java.util.List;

public interface ProductService {
  List<HJKJDProduct> searchLink(ProductQueryParam param);

  CommonPage<HJKJDProduct> search(ProductQueryParam param);

  CommonPage<HJKJDProduct> list(ProductQueryParam param);

  HJKJDProduct detail(ProductDetailParam param);

  Object link(ProductLinkParam param);

  Object fenLink(ProductLinkParam param);

  CommonPage<HJKJDProduct> hotList(ProductQueryParam param);
}
