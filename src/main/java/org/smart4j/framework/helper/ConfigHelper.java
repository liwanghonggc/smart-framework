package org.smart4j.framework.helper;

import org.smart4j.framework.ConfigCostant;
import org.smart4j.framework.util.PropsUtil;

import java.util.Properties;

/**
 * 属性文件助手类
 */
public final class ConfigHelper {

    private static final Properties CONFIG_PROPS = PropsUtil.loadProps(ConfigCostant.CONFIG_FILE);

    /**
     * 获取JDBC驱动
     */
    public static String getJdbcDriver(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.JDBC_DRIVER);
    }

    /**
     * 获取JDBC URL
     */
    public static String getJdbcUrl(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.JDBC_URL);
    }

    /**
     * 获取JDBC 用户名
     */
    public static String getJdbcUserName(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.JDBC_USERNAME);
    }

    /**
     * 获取JDBC 密码
     */
    public static String getJdbcPassword(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.JDBC_PASSWORD);
    }

    /**
     * 获取应用基础包名
     */
    public static String getAppBasePackage(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.APP_BASE_PACKAGE);
    }

    /**
     * 获取应用JSP路径
     */
    public static String getAppJspPath(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.APP_JSP_PATH, "/WEB-INF/view/");
    }

    /**
     * 获取应用静态资源路径
     */
    public static String getAppAssetPath(){
        return PropsUtil.getString(CONFIG_PROPS, ConfigCostant.APP_ASSET_PATH, "/asset/");
    }

    /**
     * 获取应用文件上传限制
     */
    public static int getAppUploadLimit(){
        return PropsUtil.getInt(CONFIG_PROPS, ConfigCostant.APP_UPLOAD_LIMIT, 10);
    }
}
