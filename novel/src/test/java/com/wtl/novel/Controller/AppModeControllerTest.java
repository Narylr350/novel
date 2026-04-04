package com.wtl.novel.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppModeControllerTest {

    @Test
    void appModeReturnsConfiguredMode() throws Exception {
        Class<?> controllerClass = Class.forName("com.wtl.novel.Controller.AppModeController");
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        ReflectionTestUtils.setField(controller, "appUiMode", "reader");

        Method appModeMethod = controllerClass.getMethod("appMode");

        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) appModeMethod.invoke(controller);

        assertEquals("reader", response.get("mode"));
    }
}
