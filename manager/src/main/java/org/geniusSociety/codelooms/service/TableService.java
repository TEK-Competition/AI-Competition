/**
 * Cealus Li 2025/8/7
 * Copyright
 */
package org.geniusSociety.codelooms.service;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.criteria.Predicate;
import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.common.base.BaseService;
import org.geniusSociety.codelooms.common.bo.BaseQuery;
import org.geniusSociety.codelooms.dao.MateTableRepository;
import org.geniusSociety.codelooms.domain.entity.MateTable;
import org.geniusSociety.codelooms.domain.vo.TableVO;
import org.geniusSociety.codelooms.mapper.TableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

/**
 * 表信息
 *
 * @author Cealus Li 2025/8/8
 */
@Service
public class TableService extends BaseService<TableVO, MateTable> {

    @Autowired
    private MateTableRepository tableRepository;

    @Autowired
    private TableMapper tableMapper;

    @Override
    protected Specification<MateTable> spec(BaseQuery query, Integer userId) {
        return (root, q, cb) -> {
            Predicate and = cb.and(cb.equal(root.get("itemId"), query.getId())
                    , cb.equal(root.get("userId"), userId));
            if (StrUtil.isNotEmpty(query.getName())) {
                Predicate name = cb.like(root.get("name"), "%" + query.getName() + "%");
                return cb.and(and, name);
            }
            return and;
        };
    }

    @Override
    protected JpaSpecificationExecutor<MateTable> getRepository() {
        return tableRepository;
    }

    @Override
    protected BaseMapper<TableVO, MateTable> getMapper() {
        return tableMapper;
    }
}
