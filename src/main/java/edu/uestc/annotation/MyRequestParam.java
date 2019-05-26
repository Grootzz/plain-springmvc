package edu.uestc.annotation;

import java.lang.annotation.*;

/**
 * 请求参数映射
 *
 * @author noodle
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    /**
     * 请求参数别名
     *
     * @return
     */
    String value() default "";
}
