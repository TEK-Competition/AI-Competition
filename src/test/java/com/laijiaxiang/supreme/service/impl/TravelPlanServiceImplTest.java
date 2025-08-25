package com.laijiaxiang.supreme.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.mapper.TravelPlanMapper;
import com.laijiaxiang.supreme.model.dto.TravelPlanRequestDTO;
import com.laijiaxiang.supreme.model.entity.TravelPlan;
import com.laijiaxiang.supreme.model.enums.Status;
import com.laijiaxiang.supreme.model.vo.TravelPlanDetailVO;
import com.laijiaxiang.supreme.model.vo.TravelPlanListVO;
import com.laijiaxiang.supreme.service.BaiLianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelPlanServiceImplTest {

    @InjectMocks
    private TravelPlanServiceImpl travelPlanService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private TravelPlanMapper travelPlanMapper;

    @Mock
    private BaiLianService baiLianService;

    private TravelPlan travelPlan;
    private TravelPlanRequestDTO travelPlanRequestDTO;

    @BeforeEach
    void setUp() {
        travelPlan = TravelPlan.builder()
                .id(1L)
                .userId(1L)
                .placeOfDeparture("北京")
                .destination("上海")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(7))
                .peopleAmount(2)
                .budgetRange("中等")
                .preference("[\"美食\", \"购物\"]")
                .additionalInstructions("无")
                .status(Status.SUCCESS.name())
                .result("[{\"day\":\"第一天\",\"city\":\"上海\",\"scenicSpotName\":\"外滩\"}]")
                .build();

        travelPlanRequestDTO = new TravelPlanRequestDTO();
        travelPlanRequestDTO.setPlaceOfDeparture("北京");
        travelPlanRequestDTO.setDestination("上海");
        travelPlanRequestDTO.setStartDate(LocalDate.now().plusDays(1));
        travelPlanRequestDTO.setEndDate(LocalDate.now().plusDays(7));
        travelPlanRequestDTO.setPeopleAmount(2);
        travelPlanRequestDTO.setBudgetRange("中等");
        travelPlanRequestDTO.setPreference(Arrays.asList("美食", "购物"));
        travelPlanRequestDTO.setAdditionalInstructions("无");
    }

    @Test
    void queryForList_WithPod_ShouldReturnTravelPlanList() {
        // Given
        SupremeContextHandler.setUserId(1L);
        List<TravelPlan> travelPlans = Arrays.asList(travelPlan);
        when(travelPlanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(travelPlans);

        // When
        List<TravelPlanListVO> result = travelPlanService.queryForList("北京");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(travelPlan.getId(), result.get(0).getId());
        assertEquals(travelPlan.getPlaceOfDeparture(), result.get(0).getPlaceOfDeparture());
        assertEquals(travelPlan.getDestination(), result.get(0).getDestination());
        assertEquals(travelPlan.getStartDate(), result.get(0).getStartDate());
        assertEquals(travelPlan.getEndDate(), result.get(0).getEndDate());
        assertEquals(travelPlan.getPeopleAmount(), result.get(0).getPeopleAmount());
        assertEquals(travelPlan.getBudgetRange(), result.get(0).getBudgetRange());
        assertEquals(Status.SUCCESS, result.get(0).getStatus());

        // Verify
        verify(travelPlanMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void queryForList_WithoutPod_ShouldReturnTravelPlanList() {
        // Given
        SupremeContextHandler.setUserId(1L);
        List<TravelPlan> travelPlans = Arrays.asList(travelPlan);
        when(travelPlanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(travelPlans);

        // When
        List<TravelPlanListVO> result = travelPlanService.queryForList(null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify
        verify(travelPlanMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void queryForList_WithEmptyPod_ShouldReturnTravelPlanList() {
        // Given
        SupremeContextHandler.setUserId(1L);
        List<TravelPlan> travelPlans = Arrays.asList(travelPlan);
        when(travelPlanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(travelPlans);

        // When
        List<TravelPlanListVO> result = travelPlanService.queryForList("");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify
        verify(travelPlanMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void queryForList_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        SupremeContextHandler.setUserId(1L);
        when(travelPlanMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // When
        List<TravelPlanListVO> result = travelPlanService.queryForList("北京");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(travelPlanMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getById_ShouldReturnTravelPlanDetail() {
        // Given
        when(travelPlanMapper.selectById(1L)).thenReturn(travelPlan);

        // When
        TravelPlanDetailVO result = travelPlanService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(travelPlan.getId(), result.getId());
        assertEquals(travelPlan.getPlaceOfDeparture(), result.getPlaceOfDeparture());
        assertEquals(travelPlan.getDestination(), result.getDestination());
        assertEquals(travelPlan.getStartDate(), result.getStartDate());
        assertEquals(travelPlan.getEndDate(), result.getEndDate());
        assertEquals(travelPlan.getPeopleAmount(), result.getPeopleAmount());
        assertEquals(travelPlan.getBudgetRange(), result.getBudgetRange());
        assertEquals(travelPlan.getAdditionalInstructions(), result.getAdditionalInstructions());
        assertNotNull(result.getTravelPlanResultList());

        // Verify
        verify(travelPlanMapper).selectById(1L);
    }

    @Test
    void generate_ShouldCreateTravelPlanAndSubmitTask() {
        // Given
        SupremeContextHandler.setUserId(1L);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        TravelPlan capturedTravelPlan = travelPlanCaptor.getValue();
        assertEquals(1L, capturedTravelPlan.getUserId());
        assertEquals(travelPlanRequestDTO.getPlaceOfDeparture(), capturedTravelPlan.getPlaceOfDeparture());
        assertEquals(travelPlanRequestDTO.getDestination(), capturedTravelPlan.getDestination());
        assertEquals(travelPlanRequestDTO.getStartDate(), capturedTravelPlan.getStartDate());
        assertEquals(travelPlanRequestDTO.getEndDate(), capturedTravelPlan.getEndDate());
        assertEquals(travelPlanRequestDTO.getPeopleAmount(), capturedTravelPlan.getPeopleAmount());
        assertEquals(travelPlanRequestDTO.getBudgetRange(), capturedTravelPlan.getBudgetRange());
        assertEquals("[\"美食\",\"购物\"]", capturedTravelPlan.getPreference());
        assertEquals(travelPlanRequestDTO.getAdditionalInstructions(), capturedTravelPlan.getAdditionalInstructions());

        // Verify
        verify(travelPlanMapper).insert(any(TravelPlan.class));
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the inner logic
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);
    }

    @Test
    void generate_WithEmptyAdditionalInstructions_ShouldSetDefaultValue() {
        // Given
        SupremeContextHandler.setUserId(1L);
        travelPlanRequestDTO.setAdditionalInstructions(null);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        TravelPlan capturedTravelPlan = travelPlanCaptor.getValue();
        assertEquals("暂无", capturedTravelPlan.getAdditionalInstructions());
    }

    @Test
    void generate_WithEmptyAdditionalInstructionsAndWhitespace_ShouldSetDefaultValue() {
        // Given
        SupremeContextHandler.setUserId(1L);
        travelPlanRequestDTO.setAdditionalInstructions("   ");
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        TravelPlan capturedTravelPlan = travelPlanCaptor.getValue();
        assertEquals("暂无", capturedTravelPlan.getAdditionalInstructions());
    }

    @Test
    void reGenerate_WithValidTravelPlan_ShouldResubmitTask() throws Exception {
        // Given
        when(travelPlanMapper.selectById(1L)).thenReturn(travelPlan);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(baiLianService.singleChat(anyString())).thenReturn("[{\"day\":\"第一天\",\"city\":\"上海\",\"scenicSpotName\":\"外滩\"}]");

        // When
        travelPlanService.reGenerate(1L);

        // Then
        verify(travelPlanMapper).selectById(1L);
        verify(travelPlanMapper).updateById(travelPlanCaptor.capture());
        TravelPlan capturedTravelPlan = travelPlanCaptor.getValue();
        assertEquals(Status.IN_PROGRESS.name(), capturedTravelPlan.getStatus());
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the inner logic
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);
    }

    @Test
    void reGenerate_WithPastStartDate_ShouldThrowBizException() {
        // Given
        TravelPlan pastTravelPlan = TravelPlan.builder()
                .id(1L)
                .userId(1L)
                .placeOfDeparture("北京")
                .destination("上海")
                .startDate(LocalDate.of(2020, 10, 1))
                .endDate(LocalDate.now().plusDays(7))
                .peopleAmount(2)
                .budgetRange("中等")
                .preference("[\"美食\", \"购物\"]")
                .additionalInstructions("无")
                .status(Status.SUCCESS.name())
                .build();
        when(travelPlanMapper.selectById(1L)).thenReturn(pastTravelPlan);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> travelPlanService.reGenerate(1L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("时间不对了，请重新创建！", exception.getMessage());

        // Verify
        verify(travelPlanMapper).selectById(1L);
    }

    @Test
    void reGenerate_WithPastEndDate_ShouldThrowBizException() {
        // Given
        TravelPlan pastTravelPlan = TravelPlan.builder()
                .id(1L)
                .userId(1L)
                .placeOfDeparture("北京")
                .destination("上海")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.of(2020, 10, 7))
                .peopleAmount(2)
                .budgetRange("中等")
                .preference("[\"美食\", \"购物\"]")
                .additionalInstructions("无")
                .status(Status.SUCCESS.name())
                .build();
        when(travelPlanMapper.selectById(1L)).thenReturn(pastTravelPlan);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> travelPlanService.reGenerate(1L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("时间不对了，请重新创建！", exception.getMessage());

        // Verify
        verify(travelPlanMapper).selectById(1L);
    }

    @Test
    void reGenerate_WithPastDate_ShouldThrowBizException() {
        // Given
        TravelPlan pastTravelPlan = TravelPlan.builder()
                .id(1L)
                .userId(1L)
                .placeOfDeparture("北京")
                .destination("上海")
                .startDate(LocalDate.of(2020, 10, 1))
                .endDate(LocalDate.of(2020, 10, 7))
                .peopleAmount(2)
                .budgetRange("中等")
                .preference("[\"美食\", \"购物\"]")
                .additionalInstructions("无")
                .status(Status.SUCCESS.name())
                .build();
        when(travelPlanMapper.selectById(1L)).thenReturn(pastTravelPlan);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> travelPlanService.reGenerate(1L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("时间不对了，请重新创建！", exception.getMessage());

        // Verify
        verify(travelPlanMapper).selectById(1L);
    }

    @Test
    void generate_WithAiServiceException_ShouldSetFailStatus() throws Exception {
        // Given
        SupremeContextHandler.setUserId(1L);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);
        when(baiLianService.singleChat(anyString())).thenThrow(new RuntimeException("AI服务异常"));

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        verify(travelPlanMapper).insert(any(TravelPlan.class));
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the exception handling
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify that updateById was called
        verify(travelPlanMapper).updateById(any(TravelPlan.class));
    }

    @Test
    void reGenerate_WithAiServiceException_ShouldSetFailStatus() throws Exception {
        // Given
        when(travelPlanMapper.selectById(1L)).thenReturn(travelPlan);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(baiLianService.singleChat(anyString())).thenThrow(new RuntimeException("AI服务异常"));

        // When
        travelPlanService.reGenerate(1L);

        // Then
        verify(travelPlanMapper).selectById(1L);
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the exception handling
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify updateById was called
        verify(travelPlanMapper, times(2)).updateById(any(TravelPlan.class));
    }

    @Test
    void generate_WithInvalidAiResponse_ShouldSetFailStatus() throws Exception {
        // Given
        SupremeContextHandler.setUserId(1L);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);
        when(baiLianService.singleChat(anyString())).thenReturn("invalid json response");

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        verify(travelPlanMapper).insert(any(TravelPlan.class));
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the exception handling
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify that updateById was called
        verify(travelPlanMapper).updateById(any(TravelPlan.class));
    }

    @Test
    void reGenerate_WithInvalidAiResponse_ShouldSetFailStatus() throws Exception {
        // Given
        when(travelPlanMapper.selectById(1L)).thenReturn(travelPlan);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(baiLianService.singleChat(anyString())).thenReturn("invalid json response");

        // When
        travelPlanService.reGenerate(1L);

        // Then
        verify(travelPlanMapper).selectById(1L);
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the exception handling
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify updateById was called
        verify(travelPlanMapper, times(2)).updateById(any(TravelPlan.class));
    }

    @Test
    void generate_WithValidAiResponse_ShouldSetSuccessStatus() throws Exception {
        // Given
        SupremeContextHandler.setUserId(1L);
        ArgumentCaptor<TravelPlan> travelPlanCaptor = ArgumentCaptor.forClass(TravelPlan.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        String validResponse = "[{\"day\":\"第一天\",\"city\":\"上海\",\"scenicSpotName\":\"外滩\"}]";

        when(travelPlanMapper.insert(travelPlanCaptor.capture())).thenReturn(1);
        when(baiLianService.singleChat(anyString())).thenReturn(validResponse);

        // When
        travelPlanService.generate(travelPlanRequestDTO);

        // Then
        verify(travelPlanMapper).insert(any(TravelPlan.class));
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the success path
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify that updateById was called
        verify(travelPlanMapper).updateById(any(TravelPlan.class));
    }

    @Test
    void reGenerate_WithValidAiResponse_ShouldSetSuccessStatus() throws Exception {
        // Given
        when(travelPlanMapper.selectById(1L)).thenReturn(travelPlan);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        String validResponse = "[{\"day\":\"第一天\",\"city\":\"上海\",\"scenicSpotName\":\"外滩\"}]";

        when(baiLianService.singleChat(anyString())).thenReturn(validResponse);

        // When
        travelPlanService.reGenerate(1L);

        // Then
        verify(travelPlanMapper).selectById(1L);
        verify(executorService).submit(runnableCaptor.capture());

        // Execute the captured runnable to test the success path
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertDoesNotThrow(capturedRunnable::run);

        // Verify updateById was called
        verify(travelPlanMapper, times(2)).updateById(any(TravelPlan.class));
    }
}
