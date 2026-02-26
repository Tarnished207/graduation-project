package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.common.Result;
import com.example.petserver.entity.PetCase;
import com.example.petserver.service.IPetCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/petCase")
public class PetCaseController {

	@Autowired
	private IPetCaseService petCaseService;

	// 新增病例
	@PostMapping("/add")
	public Result add(@RequestBody PetCase petCase) {
		petCaseService.save(petCase);
		return Result.success();
	}

	// 删除病例
	@GetMapping("/delete/{id}")
	public Result delete(@PathVariable Long id) {
		petCaseService.removeById(id);
		return Result.success();
	}

	// 根据宠物ID查询病例列表
	@GetMapping("/list/{petId}")
	public Result list(@PathVariable Long petId) {
		QueryWrapper<PetCase> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("pet_id", petId);
		queryWrapper.orderByDesc("create_time");
		List<PetCase> list = petCaseService.list(queryWrapper);
		return Result.success(list);
	}
}
