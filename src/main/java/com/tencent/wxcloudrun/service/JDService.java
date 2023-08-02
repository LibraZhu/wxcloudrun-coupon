package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.HJKJDProductQueryParam;
import com.tencent.wxcloudrun.dto.JDProductListParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.OmsOrder;

public interface JDService extends IService<OmsOrder> {
    /**
     * 同步订单
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    void syncOrder(String startTime, String endTime);


    /**
     * 同步任务
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    void syncTask(String startTime, String endTime);

    /**
     * 公众号链接转链回复
     *
     * @param request 微信消息
     * @return 微信回复消息
     */
    Object wxMessage(WxMessageRequest request);

    /**
     * 京粉商品列表
     *
     * @param param 查询条件
     * @return 商品列表
     */
    CommonPage<HJKJDProduct> listProduct(JDProductListParam param);

    /**
     * 蚂蚁星球 商品搜索
     *
     * @param param 查询条件
     * @return 商品列表
     */
    CommonPage<HJKJDProduct> searchHJKProduct(HJKJDProductQueryParam param);

    /**
     * 蚂蚁星球 京东商详
     *
     * @param id 商品id
     * @return 商详
     */
    HJKJDProduct getHJKProductDetail(String id);

    /**
     * 蚂蚁星球 京东推广链接
     *
     * @param id 商品id
     * @return 推广链接
     */
    String getHJKUnionUrl(String id);

    /**
     * 好单库 京东推广链接
     *
     * @param id         商品id
     * @param coupon_url 优惠券链接
     * @param subUnionId 子渠道
     * @return 推广链接
     */
    String getHDKUnionUrl(String id, String coupon_url, String subUnionId);
}
