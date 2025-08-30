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
import org.geniusSociety.codelooms.dao.MateTableFieldRepository;
import org.geniusSociety.codelooms.domain.entity.MateTableField;
import org.geniusSociety.codelooms.domain.vo.TableFieldVO;
import org.geniusSociety.codelooms.mapper.TableFieldMapper;
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
public class TableFieldService extends BaseService<TableFieldVO, MateTableField> {

    @Autowired
    private MateTableFieldRepository tableFieldRepository;

    @Autowired
    private TableFieldMapper tableFieldMapper;

    @Override
    protected Specification<MateTableField> spec(BaseQuery query, Integer userId) {
        return (root, q, cb) -> {
            Predicate and = cb.equal(root.get("tableId"), query.getId());
            if (StrUtil.isNotEmpty(query.getName())) {
                Predicate name = cb.like(root.get("name"), "%" + query.getName() + "%");
                return cb.and(and, name);
            }
            return and;
        };
    }

    @Override
    protected JpaSpecificationExecutor<MateTableField> getRepository() {
        return tableFieldRepository;
    }

    @Override
    protected BaseMapper<TableFieldVO, MateTableField> getMapper() {
        return tableFieldMapper;
    }
}
