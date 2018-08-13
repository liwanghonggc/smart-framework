package org.smart4j.framework.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 切面代理
 * 在切面类中,需要在目标方法被调用的前后增加相应的逻辑
 * 提供一个模板方法,在该抽象类的具体实现中扩展相应的抽象方法
 * TransactionProxy直接实现了Proxy提供了具体的事务功能
 * AspectProxy只是一个通用的切面代理,若想提供某AOP功能可继承该类
 */
public abstract class AspectProxy implements Proxy{

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectProxy.class);

    @Override
    public Object doProxy(ProxyChain proxyChain) throws Throwable {
        Object result = null;

        Class<?> cls = proxyChain.getTargetClass();
        Method method = proxyChain.getTargetMethod();
        Object[] params = proxyChain.getMethodParams();

        begin();

        try {
            if(intercept(cls, method, params)){
                before(cls, method, params);
                result = proxyChain.doProxyChain();
                after(cls, method, params, result);
            }else{
                result = proxyChain.doProxyChain();
            }
        } catch (Exception e) {
            LOGGER.error("proxy failure", e);
            error(cls, method, params, e);
            throw e;
        } finally {
            end();
        }

        return result;

    }

    public void begin(){}

    public boolean intercept(Class<?> cls, Method method, Object[] params) throws Throwable{
        return true;
    }

    public void before(Class<?> cls, Method method, Object[] params) throws Throwable{}

    public void after(Class<?> cls, Method method, Object[] params, Object result) throws Throwable{}

    public void error(Class<?> cls, Method method, Object[] params, Throwable e){}

    public void end(){}
}
