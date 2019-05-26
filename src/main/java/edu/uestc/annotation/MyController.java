package edu.uestc.annotation;


import java.lang.annotation.*;

/**
 * Controller注解
 *
 * @author noodle
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    /**
     * controller别名
     *
     * @return
     */
    String value() default "";
}
