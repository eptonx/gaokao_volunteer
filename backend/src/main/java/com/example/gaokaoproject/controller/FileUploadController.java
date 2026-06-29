package com.example.gaokaoproject.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    // 上传目录（自动创建），按实际情况修改
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String newName = UUID.randomUUID() + ext;
            File dest = new File(UPLOAD_DIR + newName);
            file.transferTo(dest);

            result.put("success", true);
            result.put("fileName", original);
            result.put("fileUrl", "http://localhost:8080/file/view/" + newName);
            result.put("fileSize", file.getSize());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 访问上传的文件（浏览器直接打开或下载）
    @GetMapping("/view/{fileName}")
    public void view(@PathVariable String fileName, jakarta.servlet.http.HttpServletResponse response) {
        try {
            File file = new File(UPLOAD_DIR + fileName);
            if (!file.exists()) {
                response.setStatus(404);
                response.getWriter().write("文件不存在");
                return;
            }
            // 根据扩展名设置 Content-Type
            String name = fileName.toLowerCase();
            if (name.endsWith(".pdf")) response.setContentType("application/pdf");
            else if (name.endsWith(".png")) response.setContentType("image/png");
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) response.setContentType("image/jpeg");
            else response.setContentType("application/octet-stream");

            java.io.OutputStream os = response.getOutputStream();
            java.nio.file.Files.copy(file.toPath(), os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
