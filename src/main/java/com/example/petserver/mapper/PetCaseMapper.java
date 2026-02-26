package com.example.petserver.mapper;

import com.example.petserver.entity.PetCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetCaseMapper extends BaseMapper<PetCase> {
}
