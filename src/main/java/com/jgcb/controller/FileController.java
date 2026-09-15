package com.jgcb.controller;

import com.jgcb.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "mp4", "webm", "mov", "m4v"
    ));

    private static final Set<String> IMAGE_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    ));

    private static final Set<String> VIDEO_EXT = new HashSet<>(Arrays.asList(
            "mp4", "webm", "mov", "m4v"
    ));

    @Value("${file.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            return Result.fail("不支持的文件格式");
        }

        try {
            String savedPath = saveFile(file, ext);
            Map<String, String> data = new HashMap<>();
            data.put("url", "/" + savedPath);
            return Result.ok(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/uploads")
    public Result<List<Map<String, String>>> multiUpload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String ext = getExtension(file.getOriginalFilename());
            if (!ALLOWED_EXT.contains(ext.toLowerCase())) continue;
            String savedPath = saveFile(file, ext);
            Map<String, String> item = new HashMap<>();
            item.put("url", "/" + savedPath);
            result.add(item);
        }
        return Result.ok(result);
    }

    private String saveFile(MultipartFile file, String ext) throws IOException {
        String dateDir = LocalDate.now().toString();
        Path dir = Paths.get(UPLOAD_DIR, dateDir).toAbsolutePath();
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString() + "." + ext;
        Path dest = dir.resolve(filename);
        file.transferTo(dest.toFile());

        return UPLOAD_DIR + dateDir + "/" + filename;
    }

    @GetMapping("/thumb")
    public ResponseEntity<byte[]> thumb(@RequestParam("url") String url,
                                        @RequestParam(value = "w", defaultValue = "600") int w) throws IOException {
        if (w < 1 || w > 4096) w = 600;

        String rel = url.startsWith("/") ? url.substring(1) : url;
        Path uploadsRoot = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path original = Paths.get(rel).toAbsolutePath().normalize();
        if (!original.startsWith(uploadsRoot) || !Files.isRegularFile(original)) {
            return ResponseEntity.notFound().build();
        }
        String ext = getExtension(original.getFileName().toString());
        boolean isImage = IMAGE_EXT.contains(ext.toLowerCase());
        boolean isVideo = VIDEO_EXT.contains(ext.toLowerCase());
        if (!isImage && !isVideo) {
            return ResponseEntity.notFound().build();
        }

        Path cacheDir = uploadsRoot.resolve(".thumbs");
        Files.createDirectories(cacheDir);
        Path cacheFile = cacheDir.resolve(original.getFileName().toString() + "_w" + w + ".jpg");

        byte[] bytes;
        if (Files.exists(cacheFile)) {
            bytes = Files.readAllBytes(cacheFile);
        } else if (isImage) {
            BufferedImage img = ImageIO.read(original.toFile());
            if (img == null) {
                return ResponseEntity.notFound().build();
            }
            BufferedImage scaled = scaleToWidth(img, w);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(scaled, "jpg", out);
            bytes = out.toByteArray();
            Files.write(cacheFile, bytes);
        } else {
            bytes = extractVideoThumb(original, w, cacheFile);
            if (bytes == null) {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(bytes);
    }

    private byte[] extractVideoThumb(Path videoFile, int w, Path cacheFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", videoFile.toString(),
                    "-frames:v", "1",
                    "-vf", "scale=" + w + ":-1",
                    "-q:v", "2",
                    cacheFile.toString()
            );
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (!Files.exists(cacheFile) || Files.size(cacheFile) == 0) {
                return null;
            }
            return Files.readAllBytes(cacheFile);
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage scaleToWidth(BufferedImage src, int maxW) {
        int w = src.getWidth();
        int h = src.getHeight();
        int targetW = w;
        int targetH = h;
        if (w > maxW) {
            targetW = maxW;
            targetH = Math.max(1, (int) Math.round((double) h * maxW / w));
        }
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetW, targetH);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i + 1) : "";
    }
}
