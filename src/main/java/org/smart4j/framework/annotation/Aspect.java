package org.smart4j.framework.annotation;

import java.lang.annotation.*;

/**
 * 切面注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

    /**
     * 该注解包含一个名为value的属性,它是一个注解类,用来定义Controller这类注解
     */
    Class<? extends Annotation> value();
}
