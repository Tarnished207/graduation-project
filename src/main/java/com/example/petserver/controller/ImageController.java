package com.example.petserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class ImageController {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		System.out.println("ImageController: Request for filename: " + filename);
		String dir = uploadDir;
		if (dir != null && !dir.endsWith("/")) {
			dir = dir + "/";
		}

		// 使用 Path/File 确保正确处理路径中的空格和特殊字符
		Path filePath = Paths.get(dir + filename);
		File file = filePath.toFile();
		System.out.println("ImageController: Resolved path: " + file.getAbsolutePath());
		System.out.println("ImageController: File exists? " + file.exists());

		if (file.exists() && file.isFile()) {
			Resource resource = new FileSystemResource(file);

			// 尝试获取文件类型
			String contentType = "application/octet-stream";
			try {
				contentType = Files.probeContentType(filePath);
			} catch (IOException e) {
				System.out.println("ImageController: Error probing content type: " + e.getMessage());
			}
			if (contentType == null) {
				// 手动根据后缀名判断，作为兜底
				if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
				else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg"))
					contentType = "image/jpeg";
				else contentType = "application/octet-stream";
			}
			System.out.println("ImageController: Content-Type detected: " + contentType);

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(resource);
		} else {
			System.out.println("ImageController: File not found");
			return ResponseEntity.notFound().build();
		}
	}
}
