package com.wayapay.thirdpartyintegrationservice.event;

import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

@Service
public class AnnotationOperation {

    public AuditPaymentOperation getAnnotation(JoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(AuditPaymentOperation.class);
    }
}
