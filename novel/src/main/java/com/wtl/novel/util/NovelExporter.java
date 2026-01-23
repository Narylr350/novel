package com.wtl.novel.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.zip.GZIPInputStream;

/**
 * 小说导出工具 - 命令行版本
 * 用法：直接运行 main 方法，修改参数即可导出指定小说为 txt 文件
 */
public class NovelExporter {

    // ==================== 数据库配置 ====================
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "novel";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "novel_root_password";
    
    // 输出目录（相对路径，相对于运行目录）
    private static final String OUTPUT_DIR = "./app/export/";

    public static void main(String[] args) {
        // 方式1：通过小说ID导出
        exportByNovelId(614L);
        
        // 方式2：通过小说名称导出（支持模糊匹配）
        // exportByNovelName("我的");
    }

    public static void exportByNovelId(Long... novelIds) {
        for (Long novelId : novelIds) {
            try {
                System.out.println("开始导出小说 ID: " + novelId);
                String novelTitle = getNovelTitleById(novelId);
                if (novelTitle == null) {
                    System.err.println("未找到小说 ID: " + novelId);
                    continue;
                }
                String outputPath = OUTPUT_DIR + sanitizeFileName(novelTitle) + "_" + novelId + ".txt";
                exportNovel(novelId, novelTitle, outputPath);
                System.out.println("导出完成: " + outputPath);
            } catch (Exception e) {
                System.err.println("导出小说 ID " + novelId + " 失败: " + e.getMessage());
            }
        }
    }

    public static void exportByNovelName(String novelName) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, title FROM novel WHERE (title LIKE ? OR true_name LIKE ?) AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + novelName + "%");
                ps.setString(2, "%" + novelName + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Long novelId = rs.getLong("id");
                        String title = rs.getString("title");
                        System.out.println("找到小说: ID=" + novelId + ", 标题=" + title);
                        String outputPath = OUTPUT_DIR + sanitizeFileName(title) + "_" + novelId + ".txt";
                        exportNovel(novelId, title, outputPath);
                        System.out.println("导出完成: " + outputPath);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("导出失败: " + e.getMessage());
        }
    }

    private static void exportNovel(Long novelId, String novelTitle, String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT chapter_number, title, content FROM chapter WHERE novel_id = ? AND is_deleted = 0 ORDER BY chapter_number");
             BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            writer.write("【" + novelTitle + "】\n");
            writer.write("==================================================\n\n");

            ps.setLong(1, novelId);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    int chapterNumber = rs.getInt("chapter_number");
                    String chapterTitle = rs.getString("title");
                    byte[] compressedContent = rs.getBytes("content");
                    String content = decompress(compressedContent);
                    content = cleanContent(content);
                    
                    writer.write("第" + chapterNumber + "章 " + chapterTitle + "\n");
                    writer.write("----------------------------------------\n");
                    writer.write(content);
                    writer.write("\n\n");
                    count++;
                    if (count % 50 == 0) {
                        System.out.println("已导出 " + count + " 章...");
                    }
                }
                System.out.println("共导出 " + count + " 章");
            }
        }
    }

    private static String getNovelTitleById(Long novelId) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT title FROM novel WHERE id = ? AND is_deleted = 0")) {
            ps.setLong(1, novelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title");
                }
            }
        }
        return null;
    }

    private static Connection getConnection() throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");
        String url = String.format(
            "jdbc:mariadb://%s:%s/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8",
            DB_HOST, DB_PORT, DB_NAME);
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    private static String decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return "";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        } catch (java.util.zip.ZipException e) {
            return new String(compressed, StandardCharsets.UTF_8);
        }
    }

    private static String cleanContent(String content) {
        if (content == null) return "";
        return content.replaceAll("[\u200B\u200C\u200D\uFEFF]", "");
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null) return "unknown";
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
