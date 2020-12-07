package com.wodongx123.butterknife_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD) // 该注解只能用于方法上
@Retention(RetentionPolicy.CLASS) // 该注解的生命周期直到编译期
public @interface OnClick {
    int[] value(); // 需要传入一个int数组，也就是onClick方法所需要的的控件id集合
}
