package com.wtl.novel.Service;

import com.wtl.novel.entity.UserAccessLog;
import com.wtl.novel.repository.UserAccessLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class UserAccessLogService {
    private static final Logger log = LoggerFactory.getLogger(UserAccessLogService.class);

    private final UserAccessLogRepository repository;
    @Value("${app.ui.mode:reader}")
    private String appUiMode;

    public UserAccessLogService(UserAccessLogRepository repository) {
        this.repository = repository;
    }

    // 全局锁管理器（静态保证所有线程共享）
    private static final ConcurrentHashMap<Long, ReentrantLock> USER_LOCKS = new ConcurrentHashMap<>();

    public void findByUserIdAndVisitDate(Long userId, String ipAddress, String userAgent) {
        // 获取或创建用户专属锁
        ReentrantLock userLock = USER_LOCKS.computeIfAbsent(userId, k -> new ReentrantLock());
        userLock.lock(); // 加锁（同一user_id在此排队）
        try {
            LocalDate now = LocalDate.now();
            List<UserAccessLog> userAccessLogs;
            try {
                userAccessLogs = repository.findByUserIdAndVisitDate(userId, now);
            } catch (InvalidDataAccessResourceUsageException exception) {
                if (isReaderModeMissingUserAccessLogs(exception)) {
                    // Lite reader packages intentionally omit user_access_logs. Skip
                    // visit tracking there so authenticated reading APIs still work.
                    log.warn("Reader mode user access logging skipped because user_access_logs is absent");
                    return;
                }
                throw exception;
            }

            if (!userAccessLogs.isEmpty()) {
                // 更新现有记录
                UserAccessLog userAccessLog = userAccessLogs.get(0);
                updateAccessLog(userAccessLog, ipAddress, userAgent);
                repository.save(userAccessLog);
            } else {
                // 创建新记录
                UserAccessLog newLog = new UserAccessLog();
                newLog.setUserId(userId);
                newLog.setIpAddress(ipAddress);
                newLog.setUserAgent(userAgent);
                newLog.setVisitDate(now);
                repository.save(newLog);
            }
        } finally {
            userLock.unlock(); // 必须确保解锁
        }
    }

    private boolean isReaderModeMissingUserAccessLogs(InvalidDataAccessResourceUsageException exception) {
        return "reader".equalsIgnoreCase(appUiMode)
                && exception.getMessage() != null
                && exception.getMessage().contains("user_access_logs");
    }

    // 工具方法：更新IP和UserAgent集合
    private void updateAccessLog(UserAccessLog log, String ip, String ua) {
        // 更新IP集合
        Set<String> ipSet = splitToSet(log.getIpAddress());
        ipSet.add(ip);
        log.setIpAddress(String.join("@", ipSet));

        // 更新UserAgent集合
        Set<String> uaSet = splitToSet(log.getUserAgent());
        uaSet.add(ua);
        log.setUserAgent(String.join("@", uaSet));
    }

    // 工具方法：拆分字符串到Set（处理null）
    private Set<String> splitToSet(String input) {
        if (input == null || input.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(input.split("@"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

}
