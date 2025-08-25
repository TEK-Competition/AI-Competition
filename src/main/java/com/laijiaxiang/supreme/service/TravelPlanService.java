package com.laijiaxiang.supreme.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laijiaxiang.supreme.model.dto.TravelPlanRequestDTO;
import com.laijiaxiang.supreme.model.entity.TravelPlan;
import com.laijiaxiang.supreme.model.vo.TravelPlanDetailVO;
import com.laijiaxiang.supreme.model.vo.TravelPlanListVO;

import java.util.List;

public interface TravelPlanService extends IService<TravelPlan> {

    List<TravelPlanListVO> queryForList(String destination);

    TravelPlanDetailVO getById(Long id);

    void generate(TravelPlanRequestDTO travelPlanRequestDTO);

    void reGenerate(Long id);

}
