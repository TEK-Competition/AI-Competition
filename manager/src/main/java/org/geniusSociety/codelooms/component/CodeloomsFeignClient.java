/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.component;

import org.geniusSociety.codelooms.domain.dto.AnswerDTO;
import org.geniusSociety.codelooms.domain.dto.ExegesisQuestionDTO;
import org.geniusSociety.codelooms.domain.dto.QuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 模型平台接口
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
@FeignClient(value = "model-service", url = "${config.model-service}")
public interface CodeloomsFeignClient {


    /**
     * 转换接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "conversion/answer")
    AnswerDTO conversion(@RequestBody QuestionDTO sp);

    /**
     * 表关系接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "relation/answer")
    AnswerDTO relation(@RequestBody QuestionDTO sp);

    /**
     * 表关系图接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "graph/answer")
    AnswerDTO graph(@RequestBody QuestionDTO sp);

    /**
     * 文件关系图接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "process/answer")
    AnswerDTO process(@RequestBody QuestionDTO sp);

    /**
     * 表字段接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "field/answer")
    AnswerDTO field(@RequestBody QuestionDTO sp);

    /**
     * 字段注释接口
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "exegesis/answer")
    AnswerDTO exegesis(@RequestBody ExegesisQuestionDTO sp);

    /**
     * 添加知识库
     *
     * @param sp
     * @return
     */
    @PostMapping(value = "exegesis/knowledge")
    void knowledge(@RequestBody ExegesisQuestionDTO sp);
}
