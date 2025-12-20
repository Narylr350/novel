package com.wtl.novel.Controller;

import com.wtl.novel.Service.PlatformApiKeyService;
import com.wtl.novel.Service.PlatformService;
import com.wtl.novel.entity.Platform;
import com.wtl.novel.entity.PlatformApiKey;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.entity.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 翻译API配置管理Controller
 * 用于管理翻译平台和API密钥
 */
@RestController
@RequestMapping("/api/translation-config")
public class TranslationConfigController {

    @Autowired
    private PlatformService platformService;

    @Autowired
    private PlatformApiKeyService platformApiKeyService;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    // ==================== 平台管理 ====================

    /**
     * 获取所有翻译平台
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<Map<String, Object>>> getAllPlatforms() {
        List<Platform> platforms = platformService.getPlatformsByType("translation");
        List<Map<String, Object>> result = platforms.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("platformName", p.getPlatformName());
            map.put("platformType", p.getPlatformType());
            
            // 获取该平台的API密钥数量
            List<PlatformApiKey> keys = platformApiKeyService.getApiKeysByPlatformId(p.getId());
            map.put("apiKeyCount", keys.size());
            
            // 获取API URL配置
            Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(p.getPlatformName());
            map.put("apiUrl", dict != null ? dict.getValueField() : "");
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 添加新平台
     */
    @PostMapping("/platforms")
    public ResponseEntity<Map<String, Object>> addPlatform(@RequestBody Map<String, String> request) {
        String platformName = request.get("platformName");
        String apiUrl = request.get("apiUrl");
        
        if (platformName == null || platformName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "平台名称不能为空"));
        }
        
        // 检查是否已存在
        Platform existing = platformService.findPlatformByPlatformName(platformName);
        if (existing != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "平台已存在"));
        }
        
        // 创建平台
        Platform platform = new Platform();
        platform.setPlatformName(platformName);
        platform.setPlatformType("translation");
        Platform saved = platformService.savePlatform(platform);
        
        // 创建API URL配置
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            Dictionary dict = new Dictionary();
            dict.setKeyField(platformName);
            dict.setValueField(apiUrl);
            dict.setIsDeleted(false);
            dictionaryRepository.save(dict);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("platformName", saved.getPlatformName());
        result.put("message", "平台添加成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 更新平台API URL
     */
    @PutMapping("/platforms/{id}/url")
    public ResponseEntity<Map<String, Object>> updatePlatformUrl(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        
        Platform platform = platformService.getPlatformById(id);
        if (platform == null) {
            return ResponseEntity.notFound().build();
        }
        
        String apiUrl = request.get("apiUrl");
        
        // 更新或创建Dictionary配置
        Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platform.getPlatformName());
        if (dict == null) {
            dict = new Dictionary();
            dict.setKeyField(platform.getPlatformName());
            dict.setIsDeleted(false);
        }
        dict.setValueField(apiUrl);
        dictionaryRepository.save(dict);
        
        return ResponseEntity.ok(Map.of("message", "API URL更新成功"));
    }

    // ==================== API密钥管理 ====================

    /**
     * 获取指定平台的所有API密钥
     */
    @GetMapping("/platforms/{platformId}/keys")
    public ResponseEntity<List<Map<String, Object>>> getApiKeys(@PathVariable Long platformId) {
        List<PlatformApiKey> keys = platformApiKeyService.getApiKeysByPlatformId(platformId);
        List<Map<String, Object>> result = keys.stream().map(k -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", k.getId());
            map.put("apiKey", maskApiKey(k.getApiKey()));
            map.put("fullApiKey", k.getApiKey()); // 完整密钥
            map.put("isDeleted", k.getIsDeleted());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 添加API密钥
     */
    @PostMapping("/platforms/{platformId}/keys")
    public ResponseEntity<Map<String, Object>> addApiKey(
            @PathVariable Long platformId,
            @RequestBody Map<String, String> request) {
        
        Platform platform = platformService.getPlatformById(platformId);
        if (platform == null) {
            return ResponseEntity.notFound().build();
        }
        
        String apiKey = request.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "API密钥不能为空"));
        }
        
        PlatformApiKey key = new PlatformApiKey(platform, apiKey.trim());
        PlatformApiKey saved = platformApiKeyService.saveApiKey(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("apiKey", maskApiKey(saved.getApiKey()));
        result.put("message", "API密钥添加成功");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 更新API密钥
     */
    @PutMapping("/keys/{id}")
    public ResponseEntity<Map<String, Object>> updateApiKey(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        PlatformApiKey key = platformApiKeyService.getApiKeyById(id);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        
        String apiKey = request.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "API密钥不能为空"));
        }
        
        key.setApiKey(apiKey.trim());
        platformApiKeyService.saveApiKey(key);
        
        return ResponseEntity.ok(Map.of("message", "API密钥更新成功"));
    }

    /**
     * 删除API密钥（软删除）
     */
    @DeleteMapping("/keys/{id}")
    public ResponseEntity<Map<String, Object>> deleteApiKey(@PathVariable Long id) {
        PlatformApiKey key = platformApiKeyService.getApiKeyById(id);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        
        key.setIsDeleted(true);
        platformApiKeyService.saveApiKey(key);
        
        return ResponseEntity.ok(Map.of("message", "API密钥已删除"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 掩码API密钥，只显示前6位和后4位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 10) {
            return "****";
        }
        return apiKey.substring(0, 6) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
