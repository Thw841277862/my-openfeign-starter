package my.openfeign.starter;

import feign.MethodMetadata;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * 主要是根据自身业务规则 重写以下相关的三个接口，在原由的功能基础上做自身业务功能的增强
 * protected abstract void processAnnotationOnClass(MethodMetadata data, Class<?> clz);
 * <p>
 * protected abstract void processAnnotationOnMethod(MethodMetadata data,Annotation annotation,Method method);
 * <p>
 * protected abstract boolean processAnnotationsOnParameter(MethodMetadata data,Annotation[] annotations,int paramIndex);
 */
public class FeignContract extends SpringMvcContract {
    @Override
    public void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        MyContract classAnnotation = findMergedAnnotation(clz, MyContract.class);
        if (classAnnotation != null) {
            data.template().header(classAnnotation.value());
        }
        super.processAnnotationOnClass(data, clz);
    }


    @Override
    public void processAnnotationOnMethod(MethodMetadata data, Annotation annotation, Method method) {
        MyContract classAnnotation = findMergedAnnotation(method, MyContract.class);
        if (classAnnotation != null) {
            data.template().header(classAnnotation.value());
        }
        super.processAnnotationOnMethod(data, annotation, method);
    }


}
