package com.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Aspect
@Component
public class ProductAop {
    @Before("execution(* com.demo.controller.ProductController.*(..))")
    public void beforeAdvice(JoinPoint joinPoint) {
        // Log controller method execution start
    }

    @After("execution(* com.demo.controller.ProductController.*(..))")
    public void afterAdvice(JoinPoint joinPoint) {
        // Log controller method execution end
    }

}
