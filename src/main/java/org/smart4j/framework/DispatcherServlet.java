package org.smart4j.framework;

import org.smart4j.framework.bean.*;
import org.smart4j.framework.helper.*;
import org.smart4j.framework.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 请求转发器
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = -1)
public class DispatcherServlet extends HttpServlet{

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //初始化相关Helper类
        HelperLoader.init();

        //获取ServletContext对象,用于注册Servlet
        ServletContext servletContext = servletConfig.getServletContext();

        //注册处理JSP的Servlet
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");

        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");

        //注册处理静态资源的默认Servlet
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");

        //上传文件初始化
        UploadHelper.init(servletContext);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //初始化
        //这样就可以在Controller类中随时调用ServletHelper封装的Servlet API了,在Service中也可以使用
        //因为所有的调用都来自同一个请求线程,DispatcherServlet是请求线程的入口,随后线程会先后来到Controller
        //与Service中,我们只需要使用ThreadLocal来确保ServletHelper对象中Request和Response对象线程安全即可
        ServletHelper.init(request, response);

        try{
            //获取请求方法与请求路径
            String requestMethod = request.getMethod().toLowerCase();
            String requestPath = request.getPathInfo();

            //跳过/favicon.ico请求,只处理普通请求
            if(requestPath.endsWith("/favicon.ico")){
                return;
            }

            //获取Action处理器
            Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
            if(handler != null){
                //获取Controller类及其bean实例
                Class<?> controllerClass = handler.getControllerClass();
                Object controllerBean = BeanHelper.getBean(controllerClass);

                //创建请求参数对象
                Param param;
                if(UploadHelper.isMultipart(request)){
                    param = UploadHelper.createParam(request);
                }else {
                    param = RequestHelper.createParam(request);
                }

                Object result;

                //调用Action方法
                Method actionMethod = handler.getActionMethod();
                if(param.isEmpty()){
                    result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
                }else {
                    result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
                }

                //处理Action返回值
                if(result instanceof View){
                    //返回jsp页面
                    handleViewResult((View)result, request, response);
                }else if(result instanceof Data){
                    handleDataResult((Data) result, response);
                }
            }
        } finally {
            ServletHelper.destroy();
        }

    }

    private void handleViewResult(View view, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        String path = view.getPath();
        if(StringUtil.isNotEmpty(path)){
            if(path.startsWith("/")){
                response.sendRedirect(request.getContextPath() + path);
            }else{
                Map<String, Object> model = view.getModel();
                for(Map.Entry<String, Object> entry : model.entrySet()){
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                request.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(request, response);
            }
        }
    }

    private void handleDataResult(Data data, HttpServletResponse response) throws IOException{
        //返回JSON数据
        Object model = data.getModel();
        if(model != null){
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            String json = JsonUtil.toJson(model);
            writer.write(json);
            writer.flush();
            writer.close();
        }
    }

}
