package com.tencent.wxcloudrun.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.wxcloudrun.model.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2023年07月12日
 */
@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {
    boolean saveOrUpdateList(@Param("list") List<OmsOrder> list);
}
