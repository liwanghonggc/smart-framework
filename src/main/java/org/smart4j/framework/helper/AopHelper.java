package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Aspect;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.proxy.AspectProxy;
import org.smart4j.framework.proxy.Proxy;
import org.smart4j.framework.proxy.ProxyManager;
import org.smart4j.framework.proxy.TransactionProxy;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 方法拦截助手类
 * 在AopHelper中,我们需要获取所有的目标类及其被拦截的切面类实例,并通过ProxyManager创建代理对象,
 * 最后将其放入Bean Map中
 */
public final class AopHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

    /**
     * 最后,在AopHelper中通过一个静态块来初始化整个Aop框架
     */
    static {
        try {
            //获取代理类与目标类集合的映射关系,形如Map<TransactionProxy.class, Set<CustomerService.class,...>>
            Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
            //进一步获取目标类与代理对象列表的映射关系,形如Map<CustomerService.class, List<new TransactionProxy()...>>
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
            //遍历这个映射关系,从中获取目标类与代理对象列表,调用ProxyManager.createProxy方法
            //获取代理对象,放入Bean Map中
            for(Map.Entry<Class<?>, List<Proxy>> targetEntry : targetMap.entrySet()){
                Class<?> targetClass = targetEntry.getKey();
                List<Proxy> proxyList = targetEntry.getValue();
                //我们需要在整个框架中使用ProxyManager来创建代理对象,即proxy,并将该对象放入框架底层的Bean Map中,该proxy会
                //替换之前的目标对象,随后才能通过IOC将被代理的对象注入到其他对象中,因此AopHelper要在IocHelper之前被加载
                Object proxy = ProxyManager.createProxy(targetClass, proxyList);
                BeanHelper.setBean(targetClass, proxy);
            }
        } catch (Exception e) {
            LOGGER.error("aop failure", e);
        }
    }

    /**
     * 获取参数aspect注解中设置的注解类,若该注解类不是Aspect类,则调用ClassHelper的
     * getClassSetByAnnotation方法获取相关类,并把这些类放入目标类集合中,最终返回这个集合
     * @param aspect
     * @return
     * @throws Exception
     */
    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception{
        Set<Class<?>> targetClassSet = new HashSet();
        Class<? extends Annotation> annotation = aspect.value();
        //此处该annotation就是Controller
        if(annotation != null && !annotation.equals(Aspect.class)){
            targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
        }
        return targetClassSet;
    }

    /**
     * 获取代理类与目标类集合的映射关系,一个代理类可对应一个或多个目标类(比如事务代理类TransactionProxy可以代理@Service标注的类)
     * 代理类需要扩展AspectProxy抽象类,还需要带有Aspect注解,只有满足这两个条件,才能根据
     * Aspect注解中所定义的注解属性去获取该注解所对应的目标类集合,然后才能建立代理对象与目标
     * 类集合的映射关系,最终返回这个映射关系
     */
    private static Map<Class<?>, Set<Class<?>>> createProxyMap() throws Exception{
        Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap();

        //添加普通切面代理
        //如chapter3中使用@Aspect(Controller.class)标注的ControllerAspect用于记录Controller中方法执行的前后时间
        addAspectProxy(proxyMap);
        //添加事务代理
        addTransactionProxy(proxyMap);

        return proxyMap;
    }

    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception{
        //先获取AspectProxy的子类
        Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);

        for(Class<?> proxyClass : proxyClassSet){
            //看子类上有没有标注Aspect注解
            if(proxyClass.isAnnotationPresent(Aspect.class)){
                Aspect aspect = proxyClass.getAnnotation(Aspect.class);
                //获取Aspect注解括号中的classSet,如@Aspect(Controller.class),说明使用了Aspect该注解是要对
                //Controller.class进行AOP代理,则获取所有的Controller类
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                proxyMap.put(proxyClass, targetClassSet);
            }
        }
    }

    private static void addTransactionProxy(Map<Class<?>, Set<Class<?>>> proxyMap){
        Set<Class<?>> serviceClassSet = ClassHelper.getClassSetByAnnotation(Service.class);
        proxyMap.put(TransactionProxy.class, serviceClassSet);
    }

    /**
     * 一旦获取了代理类与目标类集合之间的映射关系,就能
     * 根据这个关系分析出目标类与代理对象列表之间的关系
     * @param proxyMap<TransactionProxy.class, Set<CustomerService.class,...>>
     * @return
     * @throws Exception
     */
    private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception{
        Map<Class<?>, List<Proxy>> targetMap = new HashMap();
        for(Map.Entry<Class<?>, Set<Class<?>>> proxyEntry : proxyMap.entrySet()){
            //形如TransactionProxy.class
            Class<?> proxyClass = proxyEntry.getKey();
            //形如Set<CustomerService.class,...>>
            Set<Class<?>> targetClassSet = proxyEntry.getValue();

            for(Class<?> targetClass : targetClassSet){
                Proxy proxy = (Proxy) proxyClass.newInstance();
                if(targetMap.containsKey(targetClass)){
                    targetMap.get(targetClass).add(proxy);
                }else{
                    List<Proxy> proxyList = new ArrayList();
                    proxyList.add(proxy);
                    targetMap.put(targetClass, proxyList);
                }
            }
        }
        return targetMap;
    }
}
