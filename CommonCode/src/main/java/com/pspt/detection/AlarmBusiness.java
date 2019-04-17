package com.pspt.detection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AlarmBusiness {
    private static final Logger logger = Logger.getLogger(AlarmBusiness.class);

    public static void load() {
        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(Constants.PROPERTIES_NAME));
            prop.load(in);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if ("ips".equalsIgnoreCase(key)) {
                    HikCache.ipList = prop.getProperty(key).split(",");
                }
                if ("hikUsername".equalsIgnoreCase(key)) {
                    HikCache.mHikBean.setUsername(prop.getProperty("hikUsername"));
                }
                if ("hikPassword".equalsIgnoreCase(key)) {
                    HikCache.mHikBean.setPassword(prop.getProperty("hikPassword"));
                }
                if ("hikPort".equalsIgnoreCase(key)) {
                    HikCache.mHikBean.setPort(Integer.parseInt(prop.getProperty("hikPort")));
                }
                if ("uploadUrl".equalsIgnoreCase(key)) {
                    HikCache.mHikBean.setUploadUrl(prop.getProperty("uploadUrl"));
                }
                if ("Authorization".equalsIgnoreCase(key)) {
                    HikCache.mHikBean.setAuthorization(prop.getProperty("Authorization"));
                }
            }
            in.close();
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
