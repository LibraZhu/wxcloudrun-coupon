package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.ProductQueryParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.OmsOrder;

public interface TBService extends IService<OmsOrder> {
  /**
   * 同步订单
   *
   * @param startTime 开始时间
   * @param endTime 结束时间
   */
  void syncOrder(String startTime, String endTime);

  /**
   * 同步任务
   *
   * @param startTime 开始时间
   * @param endTime 结束时间
   */
  void syncTask(String startTime, String endTime);

  /**
   * 公众号链接转链回复
   *
   * @param request 微信消息
   * @param uid
   * @return 微信回复消息
   */
  Object wxMessage(WxMessageRequest request, Long uid);

  /**
   * 商品物料搜索
   *
   * @param param 查询条件
   * @return 商品列表
   */
  CommonPage<HJKJDProduct> listProduct(ProductQueryParam param);

  /**
   * 商品搜索
   *
   * @param param 查询条件
   * @return 商品列表
   */
  CommonPage<HJKJDProduct> searchProduct(ProductQueryParam param);
  /**
   * pdd 商详
   *
   * @param id 商品id
   * @return 商详
   */
  HJKJDProduct getProductDetail(String id);

  /**
   * pdd 推广链接
   *
   * @param id 商品id
   * @param uid 子渠道
   * @return 推广链接
   */
  Object getUnionUrl(String id, String uid);

  /**
   * 根据订单的会员id获取external_id(转链时候传递的uid)
   *
   * @param specialId 会员id
   * @return
   */
  String getUidBySpecialId(String specialId);
  /**
   * 根据external_id(转链时候传递的uid)获取订单的会员id
   *
   * @param uid 用户id
   * @return
   */
  String getSpecialIdByUid(String uid);
}
