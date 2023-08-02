package com.tencent.wxcloudrun.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.wxcloudrun.model.OmsOrderSync;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单同步信息表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2023年07月12日
 */
@Mapper
public interface OmsOrderSyncMapper extends BaseMapper<OmsOrderSync> {

}
