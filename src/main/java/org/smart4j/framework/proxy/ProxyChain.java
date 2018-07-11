package org.smart4j.framework.proxy;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 代理链
 */
public class ProxyChain {

    /**
     * 目标类
     */
    private final Class<?> targetClass;

    /**
     * 目标对象
     */
    private final Object targetObject;

    /**
     * 目标方法
     */
    private final Method targetMethod;

    /**
     * 方法代理,MethodProxy是CGLib开源项目提供的一个方法代理对象,在doProxyChain中被使用
     */
    private final MethodProxy methodProxy;

    /**
     * 方法参数
     */
    private final Object[] methodParams;

    /**
     * 代理列表
     */
    private List<Proxy> proxyList = new ArrayList<>();

    /**
     * 代理索引
     */
    private int proxyIndex = 0;


    public ProxyChain(Class<?> targetClass, Object targetObject, Method targetMethod,
                      MethodProxy methodProxy, Object[] methodParams, List<Proxy> proxyList) {
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodProxy = methodProxy;
        this.methodParams = methodParams;
        this.proxyList = proxyList;
    }

    public Object[] getMethodParams() {
        return methodParams;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    /**
     * proxyIndex充当代理对象的计数器,若尚未达到proxyList的上限,则从proxyList中取出相应的Proxy对象,并
     * 调用其doProxy方法.在Proxy接口的实现中会提供相应的的横切逻辑,并调用doProxyChain方法,随后将再次调
     * 用当前ProxyChain对象的doProxyChain方法,直到proxyIndex达到ProxyList的上限为止,最后调用methodProxy
     * 的invokeSuper方法,执行目标对象的业务逻辑
     * @return
     * @throws Throwable
     */
    public Object doProxyChain() throws Throwable{
        Object methodResult;
        if(proxyIndex < proxyList.size()){
            methodResult = proxyList.get(proxyIndex++).doProxy(this);
        }else{
            methodResult = methodProxy.invokeSuper(targetObject, methodParams);
        }
        return methodResult;
    }
}
