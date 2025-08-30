/**
 * Cealus Li 2025/7/5
 * Copyright
 */
package org.geniusSociety.codelooms.common.base;


import org.geniusSociety.codelooms.common.bo.BaseQuery;
import org.geniusSociety.codelooms.common.bo.PageQuery;
import org.geniusSociety.codelooms.common.dto.PageDTO;

import java.io.Serializable;
import java.util.List;

/**
 * 基础service接口
 *
 * @param <VO,BO>
 * @author Cealus Li
 * @date 2025/7/5
 */
public interface IService<VO> {


    /**
     * 通过ID获取
     *
     * @param id
     * @return
     */
    VO detail(final Long id, final Integer userId);

    /**
     * 查询列表
     *
     * @return
     */
    List<VO> list(final BaseQuery query, final Integer userId);

    /**
     * 获取页
     *
     * @param query
     * @return
     */
    PageDTO<VO> query(final PageQuery query, final Integer userId);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    void delete(final Long id, final Integer userId);
}
