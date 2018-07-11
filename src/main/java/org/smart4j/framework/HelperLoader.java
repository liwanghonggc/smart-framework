package org.smart4j.framework;

import org.smart4j.framework.helper.*;
import org.smart4j.framework.util.ClassUtil;

/**
 * 加载相应的Helper类
 */
public final class HelperLoader {

    /**
     * AopHelper需要在IocHelper之前加载,因为首先需要通过AocHelper获取代理对象,
     * 然后才能通过IOCHelper进行依赖注入
     */
    public static void init(){
        Class<?>[] classList = {ClassHelper.class, BeanHelper.class, AopHelper.class,
                IocHelper.class, ControllerHelper.class};

        for(Class<?> cls : classList){
            ClassUtil.loadClass(cls.getName());
        }
    }
}
