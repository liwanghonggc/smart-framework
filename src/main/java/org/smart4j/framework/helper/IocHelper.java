package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Inject;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 依赖注入助手类
 */
public final class IocHelper {

    //此处创建的bean的都是单例的
    static {
        //获取所有的bean类与bean实例之间的映射关系(简称Bean Map)
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if(CollectionUtil.isNotEmpty(beanMap)){
            //遍历BeanMap
            for(Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()){
                //从BeanMap中取出Bean类与Bean实例
                Class<?> beanClass = beanEntry.getKey();
                Object beanInstance = beanEntry.getValue();
                //获取bean类定义的所有成员变量(简称Bean field)
                Field[] beanFields = beanClass.getDeclaredFields();
                if(ArrayUtil.isNotEmpty(beanFields)){
                    //遍历BeanField
                    for(Field beanField : beanFields){
                        //判断当前bean Field是否带有Inject注解
                        if(beanField.isAnnotationPresent(Inject.class)){
                            //在beanMap中获取Bean field对应的实例
                            Class<?> beanFieldClass = beanField.getType();
                            Object beanFieldInstance = beanMap.get(beanFieldClass);
                            if(beanFieldInstance != null){
                                //通过反射初始化BeanField的值
                                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                            }
                        }
                    }
                }
            }
        }
    }
}
