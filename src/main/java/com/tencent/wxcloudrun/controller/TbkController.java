package com.tencent.wxcloudrun.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgOptimusMaterialRequest;
import com.taobao.api.response.TbkDgOptimusMaterialResponse;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 淘宝联盟api
 */
@RestController
@RequestMapping("/tbk")
public class TbkController {
    final Logger logger = LoggerFactory.getLogger(TbkController.class);
    @Resource
    private TaobaoProperties taobaoProperties;
    @Value("${spring.profiles.active}")
    private String env;


    private DefaultTaobaoClient getTbkClient() {
        return new DefaultTaobaoClient(taobaoProperties.getUrl(), taobaoProperties.getAppKey(), taobaoProperties.getAppSecret());
    }

    @GetMapping(value = "/material")
    public CommonResult material(String materialId, Integer page, Integer pageSize) {
        try {
            TaobaoClient client = getTbkClient();
            TbkDgOptimusMaterialRequest req = new TbkDgOptimusMaterialRequest();
            req.setPageNo(page != null ? page : 1L);
            req.setPageSize(pageSize != null ? pageSize : 20L);
            req.setAdzoneId(taobaoProperties.getPid());
            req.setMaterialId(Long.valueOf(materialId));
            TbkDgOptimusMaterialResponse rsp = client.execute(req);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "tbk material", JSONUtil.toJsonPrettyStr(req), JSONUtil.toJsonPrettyStr(rsp));
            }
            return CommonResult.success(CommonPage.page(page, pageSize, rsp.getTotalCount(), rsp.getResultList()));
        } catch (ApiException e) {
            e.printStackTrace();
            return CommonResult.failed(e.getLocalizedMessage());
        }
    }
}
