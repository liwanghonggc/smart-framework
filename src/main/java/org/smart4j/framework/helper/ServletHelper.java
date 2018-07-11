package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet助手类
 * 不想将DispatcherServlet中的Request和Response对象传递到Controller的Action方法中,想解耦
 * 需要提供一个线程安全的对象,通过它来封装Request和Response对象,这样每个请求线程独自拥有一份
 * Request和Response对象,并且该类提供一系列常用的Servlet的API
 */
public final class ServletHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletHelper.class);

    /**
     * 使每个线程独自拥有一份ServletHelper实例
     */
    private static final ThreadLocal<ServletHelper> SERVLET_HELPER_HOLDER = new ThreadLocal<>();

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ServletHelper(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 初始化,ServletHelper开发完毕后,将其与DispatcherServlet整合,就是调用其init和destroy方法
     */
    public static void init(HttpServletRequest request, HttpServletResponse response){
        SERVLET_HELPER_HOLDER.set(new ServletHelper(request, response));
    }

    /**
     * 销毁
     */
    public static void destroy(){
        SERVLET_HELPER_HOLDER.remove();
    }

    /**
     * 获取Request对象
     */
    private static HttpServletRequest getRequest(){
        return SERVLET_HELPER_HOLDER.get().request;
    }

    /**
     * 获取Response对象
     */
    private static HttpServletResponse getResponse(){
        return SERVLET_HELPER_HOLDER.get().response;
    }

    /**
     * 获取Session对象
     */
    private static HttpSession getSession(){
        return getRequest().getSession();
    }

    /**
     * 获取ServletContext对象
     */
    private static ServletContext getServletContext(){
        return getRequest().getServletContext();
    }

    /**
     * 将属性放入Request中
     */
    public static void setRequestAttribute(String key, Object value){
        getRequest().setAttribute(key, value);
    }

    /**
     * 从Request中获取属性
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRequestAttribute(String key){
        return (T) getRequest().getAttribute(key);
    }

    /**
     * 从Request移除属性
     */
    public static void removeRequestAttribute(String key){
        getRequest().removeAttribute(key);
    }

    /**
     * 发送重定向响应
     */
    public static void sendRedirect(String location){
        try {
            getResponse().sendRedirect(getRequest().getContextPath() + location);
        } catch (IOException e) {
            LOGGER.error("redirect failure", e);
        }
    }

    /**
     * 将属性放入Session中
     */
    public static void setSessionAttribute(String key, Object value){
        getSession().setAttribute(key, value);
    }

    /**
     * 从Session中获取属性
     */
    @SuppressWarnings("unchecked")
    public static void removeSessionAttribute(String key){
        getRequest().getSession().removeAttribute(key);
    }

    /**
     * 使Session失效
     */
    public static void invalidateSession(){
        getRequest().getSession().invalidate();
    }

}
