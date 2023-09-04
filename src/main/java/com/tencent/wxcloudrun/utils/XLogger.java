package com.tencent.wxcloudrun.utils;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;

public class XLogger {
    public static void log(Logger logger, String env, String s, Object... objects) {
        if(StrUtil.equals(env, "dev")) {
            logger.info(s, objects);
        }
    }
}
