package org.smart4j.framework;

/**
 * 提供相关配置项常量
 */
public interface ConfigCostant {

    String CONFIG_FILE = "smart.properties";

    /**
     * 数据库连接信息
     */
    String JDBC_DRIVER = "smart.framework.jdbc.driver";
    String JDBC_URL = "smart.framework.jdbc.url";
    String JDBC_USERNAME = "smart.framework.jdbc.username";
    String JDBC_PASSWORD = "smart.framework.jdbc.password";

    /**
     * 类、资源文件路径信息
     */
    String APP_BASE_PACKAGE = "smart.framework.app.base_package";
    String APP_JSP_PATH = "smart.framework.app.jsp_path";
    String APP_ASSET_PATH = "smart.framework.app.asset_path";

    /**
     * 文件上传限制
     */
    String APP_UPLOAD_LIMIT = "smart.framework.app.upload_limit";
}
