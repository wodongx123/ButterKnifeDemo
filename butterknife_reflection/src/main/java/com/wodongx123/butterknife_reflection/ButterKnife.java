package com.wodongx123.butterknife_reflection;

import java.lang.reflect.Method;

public class ButterKnife {
    public static void bind(Object clazz){
        String name = clazz.getClass().getName(); // 获得类名，也就是Activity的名字（包括包名）
        String bindViewName = name + "$$ViewBinder";

        try {
            // 通过ClassName找到对应的java文件
            Class<?> c = Class.forName(bindViewName);
            // 获取到bind方法
            Method method = c.getMethod("bind", clazz.getClass());
            // 生成新的实例
            Object o = c.newInstance();
            // 调用bind方法，第一个参数是使用的实例，第二个参数是传入的参数（也就是Activity）
            method.invoke(o, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}