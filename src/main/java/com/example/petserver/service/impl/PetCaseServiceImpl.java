package com.example.petserver.service.impl;

import com.example.petserver.entity.PetCase;
import com.example.petserver.mapper.PetCaseMapper;
import com.example.petserver.service.IPetCaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PetCaseServiceImpl extends ServiceImpl<PetCaseMapper, PetCase> implements IPetCaseService {
}
