package com.wodongx123.butterknife_complier;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * @description： 一个类内部所包括的所有节点
 */
public class ElementsForType {

    // 类内部所有标记了BindView的成员变量集合
    List<VariableElement> bindViewElements;
    // 类内部所有标记了OnCLick的方法集合
    List<ExecutableElement> onClickElements;

    /**
     * 构造方法
     */
    public ElementsForType() {
        bindViewElements = new ArrayList<>();
        onClickElements = new ArrayList<>();
    }

    /**
     * 添加被BindView标记的成员变量
     * @param element
     */
    public ElementsForType addBindView(VariableElement element){
        bindViewElements.add(element);
        return this;
    }

    /**
     * 添加被OnClick标记的方法
     * @param element
     */
    public ElementsForType addOnClick(ExecutableElement element){
        onClickElements.add(element);
        return this;
    }

    public void setBindViewElements(List<VariableElement> bindViewElements) {
        this.bindViewElements = bindViewElements;
    }

    public void setOnClickElements(List<ExecutableElement> onClickElements) {
        this.onClickElements = onClickElements;
    }


    public List<VariableElement> getBindViewElements() {
        return bindViewElements;
    }

    public List<ExecutableElement> getOnClickElements() {
        return onClickElements;
    }
}
