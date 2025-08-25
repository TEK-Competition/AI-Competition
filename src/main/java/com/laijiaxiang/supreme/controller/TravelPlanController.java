package com.laijiaxiang.supreme.controller;

import com.laijiaxiang.supreme.model.dto.TravelPlanRequestDTO;
import com.laijiaxiang.supreme.model.response.SupremeResponse;
import com.laijiaxiang.supreme.model.vo.TravelPlanDetailVO;
import com.laijiaxiang.supreme.model.vo.TravelPlanListVO;
import com.laijiaxiang.supreme.service.TravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "旅行计划相关Api")
@RestController
@RequestMapping(value = {"/travelPlan"})
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @Autowired
    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @Operation(summary = "获取列表")
    @GetMapping(value = "/list")
    @ResponseBody
    public SupremeResponse<List<TravelPlanListVO>> queryForList(@RequestParam(value = "destination", required = false) String destination) {
        List<TravelPlanListVO> result = travelPlanService.queryForList(destination);
        return SupremeResponse.success(result);
    }

    @Operation(summary = "根据id获取详情")
    @GetMapping(value = "/{id}")
    @ResponseBody
    public SupremeResponse<TravelPlanDetailVO> getById(@PathVariable("id") Long id) {
        TravelPlanDetailVO result = travelPlanService.getById(id);
        return SupremeResponse.success(result);
    }

    @Operation(summary = "生成旅行计划")
    @PostMapping("/generate")
    @ResponseBody
    public SupremeResponse<String> generate(@Valid @RequestBody TravelPlanRequestDTO travelPlanRequestDTO) {
        travelPlanService.generate(travelPlanRequestDTO);
        return SupremeResponse.success("正在生成...");
    }

    @Operation(summary = "重新生成旅行计划")
    @PostMapping("/generate/{id}")
    @ResponseBody
    public SupremeResponse<String> reGenerate(@PathVariable("id") Long id) {
        travelPlanService.reGenerate(id);
        return SupremeResponse.success("正在生成...");
    }

}
