package com.example.petserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.petserver.entity.HealthRecord;
import com.example.petserver.service.IHealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * <p>
 * 健康记录表 前端控制器
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/healthRecord")
public class HealthRecordController {

	@Autowired
	private IHealthRecordService healthRecordService;

	//新增健康记录 (比如：刚打完疫苗)
	@PostMapping("/add")
	public boolean add(@RequestBody HealthRecord healthRecord) {
		return healthRecordService.save(healthRecord);
	}

	//查看某只宠物的病历本
	@GetMapping("/list/{petId}")
	public List<HealthRecord> list(@PathVariable Long petId) {
		return healthRecordService.getByPetId(petId);
	}

	//删除记录
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return healthRecordService.removeById(id);
	}
}
