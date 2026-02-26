package com.example.petserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@PostMapping("/upload")
	public String upload(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return "上传失败，文件为空";
		}

		String originalFilename = file.getOriginalFilename();
		String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
		String dir = uploadDir;
		if (dir != null && !dir.endsWith("/")) {
			dir = dir + "/";
		}
		File dest = new File((dir == null ? "" : dir) + fileName);

		if (!dest.getParentFile().exists()) {
			dest.getParentFile().mkdirs();
		}

		try {
			file.transferTo(dest);
			return "/images/" + fileName;
		} catch (IOException e) {
			e.printStackTrace();
			return "上传失败：" + e.getMessage();
		}
	}
}
