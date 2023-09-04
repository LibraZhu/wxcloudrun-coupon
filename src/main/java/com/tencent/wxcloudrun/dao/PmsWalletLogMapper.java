package com.tencent.wxcloudrun.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.wxcloudrun.model.PmsWalletLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 钱包变动日志 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Mapper
public interface PmsWalletLogMapper extends BaseMapper<PmsWalletLog> {

}
