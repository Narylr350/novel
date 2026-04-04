package com.wtl.novel.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AppModeController {

    @Value("${app.ui.mode:reader}")
    private String appUiMode;

    @GetMapping("/app-mode")
    public Map<String, String> appMode() {
        // Keep the mode endpoint on the auth-excluded path so the SPA can bootstrap before login.
        return Map.of("mode", appUiMode);
    }
}
