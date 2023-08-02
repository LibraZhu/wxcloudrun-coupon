package com.tencent.wxcloudrun.utils;

import org.slf4j.Logger;

public class XLogger {
    public static void log(Logger logger, String s, Object... objects) {
        logger.info(s, objects);
    }
}
