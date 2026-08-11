package com.example.demo.servlet;

import com.example.demo.config.AppKeys;
import com.example.demo.service.BusinessException;
import com.example.demo.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
@WebServlet({"/lab/files/download", "/lab/files/upload"})
public class FileServlet extends BaseServlet {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "text/plain", "application/pdf");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "txt", "pdf");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/lab/files/download".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=javaweb-demo-icon.png");
        try (InputStream input = getServletContext().getResourceAsStream("/icon.png");
             OutputStream output = response.getOutputStream()) {
            if (input == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            IOUtils.copy(input, output);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            Part part = request.getPart("demoFile");
            saveUpload(part);
            Flash.success(request, "文件上传成功，已安全保存到服务器临时目录");
        } catch (BusinessException | IllegalStateException ex) {
            Flash.error(request, ex.getMessage());
        }
        redirect(request, response, "/lab");
    }

    private Path saveUpload(Part part) throws IOException {
        if (part == null || part.getSize() == 0) throw new BusinessException("请选择要上传的文件");
        if (!ALLOWED_TYPES.contains(part.getContentType())) throw new BusinessException("仅允许 PNG、JPG、TXT 或 PDF 文件");
        String original = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName();
        int dot = original.lastIndexOf('.');
        String extension = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) throw new BusinessException("文件扩展名不受支持");

        Path uploadPath = (Path) getServletContext().getAttribute(AppKeys.UPLOAD_PATH);
        Path target = uploadPath.resolve(UUID.randomUUID() + "." + extension).normalize();
        if (!target.startsWith(uploadPath)) throw new BusinessException("非法文件路径");
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }
}
