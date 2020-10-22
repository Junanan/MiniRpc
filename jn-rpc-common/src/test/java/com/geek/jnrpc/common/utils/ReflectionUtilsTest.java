package com.geek.jnrpc.common.utils;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ReflectionUtilsTest {

    @Test
    public void newInstance() {

        TestClass testClass = ReflectionUtils.newInstance(TestClass.class);
        assertNotNull(testClass);
    }

    @Test
    public void getPublicMethods() {
        Method[] methods = ReflectionUtils.getPublicMethods(TestClass.class);
        assertEquals(1, methods.length);
        String name = methods[0].getName();
        assertEquals("b", name);
    }

    @Test
    public void invoke() {
        Method[] methods = ReflectionUtils.getPublicMethods(TestClass.class);
        Method b = methods[0];
//        TestClass testClass = new TestClass();
        TestClass testClass = ReflectionUtils.newInstance(TestClass.class);
        Object invoke = ReflectionUtils.invoke(testClass, b);
        assertEquals("b", invoke);

    }
}