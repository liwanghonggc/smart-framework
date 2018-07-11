package org.smart4j.framework.proxy;

/**
 * 代理接口
 */
public interface Proxy {

    /**
     * 执行链式代理,传入一个ProxyChain,用于执行链式代理
     * 所谓链式代理,就是可以将多个代理通过一条链子串起来,
     * 一个个地取执行,执行顺序取决于添加到链上的先后顺序
     */
    Object doProxy(ProxyChain proxyChain) throws Throwable;
}
