package com.laijiaxiang.supreme.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.mapper.TravelPlanMapper;
import com.laijiaxiang.supreme.model.entity.SupremePrompt;
import com.laijiaxiang.supreme.model.dto.TravelPlanRequestDTO;
import com.laijiaxiang.supreme.model.entity.TravelPlan;
import com.laijiaxiang.supreme.model.enums.Status;
import com.laijiaxiang.supreme.model.vo.TravelPlanDetailVO;
import com.laijiaxiang.supreme.model.vo.TravelPlanListVO;
import com.laijiaxiang.supreme.service.BaiLianService;
import com.laijiaxiang.supreme.service.TravelPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements TravelPlanService {

    private final ExecutorService executorService;

    private final TravelPlanMapper travelPlanMapper;
    private final BaiLianService baiLianService;

    @Autowired
    public TravelPlanServiceImpl(ExecutorService executorService,
                                 TravelPlanMapper travelPlanMapper, BaiLianService baiLianService) {
        this.executorService = executorService;
        this.travelPlanMapper = travelPlanMapper;
        this.baiLianService = baiLianService;
    }

    @Override
    public List<TravelPlanListVO> queryForList(String destination) {
        Long userId = SupremeContextHandler.getUserId();
        LambdaQueryWrapper<TravelPlan> travelPlanLambdaQueryWrapper = new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId)
                .eq(TravelPlan::getDeleted, false);
        if(StringUtils.hasText(destination)){
            travelPlanLambdaQueryWrapper.like(TravelPlan::getDestination, destination);
        }
        travelPlanLambdaQueryWrapper.orderByDesc(TravelPlan::getUpdatedAt);
        List<TravelPlan> travelPlanList = travelPlanMapper.selectList(
                travelPlanLambdaQueryWrapper
        );
        List<TravelPlanListVO> travelPlanListVOList = new ArrayList<>();
        for (TravelPlan travelPlan : travelPlanList) {
            travelPlanListVOList.add(
                    TravelPlanListVO.builder()
                            .id(travelPlan.getId())
                            .placeOfDeparture(travelPlan.getPlaceOfDeparture())
                            .destination(travelPlan.getDestination())
                            .startDate(travelPlan.getStartDate())
                            .endDate(travelPlan.getEndDate())
                            .peopleAmount(travelPlan.getPeopleAmount())
                            .budgetRange(travelPlan.getBudgetRange())
                            .preference(JSON.parseArray(travelPlan.getPreference(), String.class))
                            .status(Status.valueOf(travelPlan.getStatus()))
                            .build()
            );
        }
        return travelPlanListVOList;
    }

    @Override
    public TravelPlanDetailVO getById(Long id) {
        TravelPlan travelPlan = travelPlanMapper.selectById(id);
        TravelPlanDetailVO travelPlanDetailVO = TravelPlanDetailVO.builder()
                .id(travelPlan.getId())
                .placeOfDeparture(travelPlan.getPlaceOfDeparture())
                .destination(travelPlan.getDestination())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .peopleAmount(travelPlan.getPeopleAmount())
                .budgetRange(travelPlan.getBudgetRange())
                .preference(JSON.parseArray(travelPlan.getPreference(), String.class))
                .additionalInstructions(travelPlan.getAdditionalInstructions())
                .build();
        String result = travelPlan.getResult();
        travelPlanDetailVO.setTravelPlanResultList(result);
        return travelPlanDetailVO;
    }

    @Override
    public void generate(TravelPlanRequestDTO travelPlanRequestDTO) {
        Long userId = SupremeContextHandler.getUserId();
        TravelPlan travelPlan = TravelPlan.builder()
                .userId(userId)
                .placeOfDeparture(travelPlanRequestDTO.getPlaceOfDeparture())
                .destination(travelPlanRequestDTO.getDestination())
                .startDate(travelPlanRequestDTO.getStartDate())
                .endDate(travelPlanRequestDTO.getEndDate())
                .peopleAmount(travelPlanRequestDTO.getPeopleAmount())
                .budgetRange(travelPlanRequestDTO.getBudgetRange())
                .preference(JSON.toJSONString(travelPlanRequestDTO.getPreference()))
                .additionalInstructions(StringUtils.hasText(travelPlanRequestDTO.getAdditionalInstructions()) ? travelPlanRequestDTO.getAdditionalInstructions() : "暂无")
                .build();
        travelPlanMapper.insert(travelPlan);
        SupremePrompt supremePrompt = SupremePrompt.builder()
                .placeOfDeparture(travelPlan.getPlaceOfDeparture())
                .destination(travelPlan.getDestination())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .peopleAmount(travelPlan.getPeopleAmount())
                .budgetRange(travelPlan.getBudgetRange())
                .preference(travelPlan.getPreference())
                .additionalInstructions(travelPlan.getAdditionalInstructions())
                .build();
        String prompt = supremePrompt.toPrompt();
        log.info("提示词：{}", prompt);
        executorService.submit(() -> {
            try {
                String aiResponse = baiLianService.singleChat(prompt);
                log.info("AI返回结果：{}", aiResponse);
                //试一下是否能转成json对象，如果不能就设置成失败状态，让用户重新生成。
                TravelPlanDetailVO.checkResult(aiResponse);
                travelPlan.setResult(aiResponse);
                travelPlan.setStatus(Status.SUCCESS.name());
            } catch (Exception e) {
                log.error("AI返回结果转换失败：", e);
                travelPlan.setStatus(Status.FAIL.name());
            }
            travelPlanMapper.updateById(travelPlan);
        });

    }

    @Override
    public void reGenerate(Long id) {
        TravelPlan travelPlan = travelPlanMapper.selectById(id);
        LocalDate startDate = travelPlan.getStartDate();
        LocalDate endDate = travelPlan.getEndDate();
        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())) {
            throw new BizException(HttpStatus.BAD_REQUEST, "时间不对了，请重新创建！");
        }
        SupremePrompt supremePrompt = SupremePrompt.builder()
                .placeOfDeparture(travelPlan.getPlaceOfDeparture())
                .destination(travelPlan.getDestination())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .peopleAmount(travelPlan.getPeopleAmount())
                .budgetRange(travelPlan.getBudgetRange())
                .preference(travelPlan.getPreference())
                .additionalInstructions(travelPlan.getAdditionalInstructions())
                .build();
        String prompt = supremePrompt.toPrompt();
        log.info("提示词：{}", prompt);
        travelPlan.setStatus(Status.IN_PROGRESS.name());
        travelPlanMapper.updateById(travelPlan);
        executorService.submit(() -> {
            try {
                String aiResponse = baiLianService.singleChat(prompt);
                log.info("AI返回结果：{}", aiResponse);
                //试一下是否能转成json对象，如果不能就设置成失败状态，让用户重新生成。
                TravelPlanDetailVO.checkResult(aiResponse);
                travelPlan.setResult(aiResponse);
                travelPlan.setStatus(Status.SUCCESS.name());
            } catch (Exception e) {
                log.error("AI返回结果转换失败：", e);
                travelPlan.setStatus(Status.FAIL.name());
            }
            travelPlanMapper.updateById(travelPlan);
        });
    }

}
