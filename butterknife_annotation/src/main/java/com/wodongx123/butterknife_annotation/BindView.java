package com.wodongx123.butterknife_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD) // 只用于成员变量上
@Retention(RetentionPolicy.CLASS) // 该注解的生命周期直到编译期
public @interface BindView {
    int value(); // 使用该注解需要传入一个int值，也就是我们控件的id值
}
