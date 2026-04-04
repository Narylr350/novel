package com.wtl.novel.Service;

import com.wtl.novel.entity.Platform;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.PlatformRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlatformService {


    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Value("${app.ui.mode:reader}")
    private String appUiMode;


    public List<Platform> getAllPlatforms() {
        return platformRepository.findAll();
    }

    public Platform getPlatformById(Long id) {
        return platformRepository.findById(id).orElse(null);
    }

    public Platform savePlatform(Platform platform) {
        return platformRepository.save(platform);
    }

    public Platform findPlatformByPlatformName(String platformName) {
        return platformRepository.findPlatformByPlatformName(platformName);
    }

    public void deletePlatform(Long id) {
        platformRepository.deleteById(id);
    }

    public List<Platform> getPlatformsByType(String platformType) {
        // Lite reader packages may not ship the platform table, so derive the
        // visible reader platforms from live novel data in reader mode.
        if ("reader".equalsIgnoreCase(appUiMode) && "novel".equals(platformType)) {
            return novelRepository.findDistinctPlatforms().stream()
                    .map(platformName -> {
                        Platform platform = new Platform();
                        platform.setPlatformName(platformName);
                        platform.setPlatformType(platformType);
                        return platform;
                    })
                    .collect(Collectors.toList());
        }
        return platformRepository.findPlatformsByPlatformType(platformType);
    }
}
