package com.wodongx123.butterknife_complier;

import com.google.auto.service.AutoService;
import com.wodongx123.butterknife_annotation.BindView;
import com.wodongx123.butterknife_annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理器，用于处理注解
 *
 */
@AutoService(Processor.class)
public class AnnotationComplier extends AbstractProcessor {

    /**
     * 初始化方法，这个方法会传入一个工具类ProcessingEnvironment
     * 在注解处理器中，我们不能通过打断点的方法进行调试，也不能使用Log
     * 想要查看日志信息，只能通过ProcessingEnvironment类下提供的API
     * 不过，abstractProcessor中有一个potect类型的processingEnv变量，执行super.init就会将传入的值直接复制给它，我们可以直接使用
     * @param processingEnvironment 处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "测试日志"); // 第一个参数是日志的类型，第二个是日志的内容
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 将所有的节点按类区分，保存在map中
        Map<TypeElement, ElementsForType> map = findAndParserTarget(roundEnvironment);
        // 根据分类好的类与类内节点集合，创建不同的java文件
        createFiles(map);

        return false;
    }

    /**
     * 创建所有我们所需要的的文件
     * @param map 类与类节点的实体
     */
    private void createFiles(Map<TypeElement, ElementsForType> map) {
        if (map.isEmpty())
            return;

        // 这里用文件读写，但是可以用Java Poet(一行一行的写代码)
        Writer writer = null;
        Filer filer = processingEnv.getFiler();
        // 遍历整个map，生成多个文件
        for (TypeElement typeElement : map.keySet()) {
            // 类内所有节点实体，也就是该类下被BindView和OnClick注释的所有变量和方法
            ElementsForType elementsForType = map.get(typeElement);
            // 获取到类名
            String className = typeElement.getSimpleName().toString();
            // 创建新文件后，新文件的名字，这里就仿造ButterKnife的源码，后面添加$$ViewBinder
            String newClassName = className + "$$ViewBinder";
            // 通过方法得到包名
            String packageName = getPackageName(typeElement);

            try {
                // 创建文件
                JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + newClassName);
                // 文件写内容用的writer实体
                writer = sourceFile.openWriter();
                // 通过方法得到我们所需要的写文件所需要的的字符串
                StringBuffer content = getString(packageName, newClassName, typeElement, elementsForType);
                // 将内容写入文件中
                writer.write(content.toString());
                // 关闭文件流
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拼装我们所需要的的字符串
     * @param packageName 包名
     * @param newClassName 类名
     * @param typeElement 类节点
     * @param elementsForType 类内节点集合
     * @return
     */
    private StringBuffer getString(String packageName, String newClassName, TypeElement typeElement, ElementsForType elementsForType) {
        StringBuffer stringBuffer = new StringBuffer();

        // 首先，在最上面一行添加一个包名
        stringBuffer.append("package " + packageName + ";\n\n");
        // 添加视图的import，后面那个是大写的V
        stringBuffer.append("import android.view.View;\n\n");
        // 类名
        stringBuffer.append("public class " + newClassName + " {\n");
        // 方法名,注意类的名称要带包名，不然索引不到
        stringBuffer.append("    public void bind(final " + typeElement.getQualifiedName() + " target) {\n\n");

        // 遍历整个被BindView注解的变量，添加代码
        // target.控件名 = (控件类型)target.findViewById(资源id);
        List<VariableElement> bindViewElements = elementsForType.getBindViewElements();
        for (VariableElement element : bindViewElements){
            // 得到控件名
            Name simpleName = element.getSimpleName();
            // 得到资源id
            int resId = element.getAnnotation(BindView.class).value();
            // 得到控件的类型，做强制类型转换（高版本Android可不做，这里是为了兼容低版本）
            TypeMirror typeMirror = element.asType();

            stringBuffer.append("        target." + simpleName + " = (" +  typeMirror + ")target.findViewById(" + resId + ");\n");
        }

        // 遍历整个被OnClick注解的方法，添加代码
        // 这里注意，要套两层循环
        /**
         * target.findViewById(资源id).setOnClickListener(new View.OnClickListener() {
         *     @override
         *     public void onClick(View v) {
         *         target.方法名(v);
         *     }
         * });
         */
        List<ExecutableElement> onClickElements = elementsForType.getOnClickElements();
        for (ExecutableElement onClickElement : onClickElements) {
            // 获得方法名
            String methodName = onClickElement.getSimpleName().toString();
            // 获得资源id集合
            int[] resIds = onClickElement.getAnnotation(OnClick.class).value();
            for (int resId : resIds){
                stringBuffer.append("        target.findViewById(" + resId + ").setOnClickListener(new View.OnClickListener() {\n");
                stringBuffer.append("            @Override\n");
                stringBuffer.append("            public void onClick(View v) {\n");
                stringBuffer.append("                target." + methodName + "(v);\n");
                stringBuffer.append("            }\n");
                stringBuffer.append("        });\n");
            }
        }

        // 方法的另一半花括号
        stringBuffer.append("    }\n");
        // 类的另一半花括号
        stringBuffer.append("}\n");

        return stringBuffer;
    }

    /**
     * 通过传入的类节点，得到包名（每个类都一定在包下）
     * @param typeElement 类节点
     * @return
     */
    private String getPackageName(TypeElement typeElement) {
        PackageElement element = processingEnv.getElementUtils().getPackageOf(typeElement);
        Name elementName = element.getQualifiedName();
        return elementName.toString();
    }


    /**
     * 按类区分所有的节点
     * @param roundEnvironment
     * @return
     */
    private Map<TypeElement, ElementsForType> findAndParserTarget(RoundEnvironment roundEnvironment) {
        Map<TypeElement, ElementsForType> map = new HashMap<>();

        // 所有被BindView注解的节点集合，接下来将他们按类区分
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewElements) {
            // 检测当前的element是否是成员变量
            if (!(element instanceof VariableElement)) continue;
            // 做一下强制类型转换
            VariableElement variableElement = (VariableElement)element;
            // 获得element的父节点，一般来说，成员变量的父节点都是当前的类
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

            // 将成员变量存入map中
            if (!map.containsKey(typeElement)){
                ElementsForType elementsForType = new ElementsForType();
                elementsForType.addBindView(variableElement);
                map.put(typeElement, elementsForType);
            }else {
                ElementsForType elementsForType = map.get(typeElement);
                elementsForType.addBindView(variableElement);
            }
        }

        // 内容等同于上面的BindView，不写注释了
        Set<? extends Element> onClickElement = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        for (Element element : onClickElement) {
            if (!(element instanceof ExecutableElement)) continue;
            ExecutableElement executableElement = (ExecutableElement)element;
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();

            if (!map.containsKey(typeElement)){
                ElementsForType elementsForType = new ElementsForType();
                elementsForType.addOnClick(executableElement);
                map.put(typeElement, elementsForType);
            }else {
                ElementsForType elementsForType = map.get(typeElement);
                elementsForType.addOnClick(executableElement);
            }
        }

        return map;
    }

    /**
     * 声明注解处理器能够处理的注解有哪些
     * @return 注解的集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName()); // 获取BindView的包名+类名
        set.add(OnClick.class.getCanonicalName());
        logUtil(set.toString());
        return set;
    }

    /**
     * 声明注解处理器所支持的Java版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        logUtil("getSupportedSouceVersion");
        return processingEnv.getSourceVersion();
    }

    /**
     * 打日志
     * @param log
     */
    public void logUtil(String log){
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, log);
    }
}
