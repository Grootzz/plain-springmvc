package edu.uestc.annotation;

import java.lang.annotation.*;

/**
 * 处理器映射器
 *
 * @author nooodle
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    /**
     * 访问该方法的url
     *
     * @return
     */
    String value() default "";
}
