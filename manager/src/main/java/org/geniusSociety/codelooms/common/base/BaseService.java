/**
 * Cealus Li 2025/7/13
 * Copyright
 */
package org.geniusSociety.codelooms.common.base;

import cn.hutool.core.util.StrUtil;
import jakarta.persistence.criteria.Predicate;
import org.geniusSociety.codelooms.common.bo.BaseQuery;
import org.geniusSociety.codelooms.common.bo.PageQuery;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.dto.PageDTO;
import org.geniusSociety.codelooms.common.entity.BaseEntity;
import org.geniusSociety.codelooms.common.util.AssertUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基础service
 *
 * @author Cealus Li
 * @date 2025/7/13
 */
public abstract class BaseService<VO, DO extends BaseEntity> implements IService<VO> {

    protected abstract JpaSpecificationExecutor<DO> getRepository();

    protected abstract BaseMapper<VO, DO> getMapper();

    /**
     * 获取详情
     *
     * @param id
     * @param userId
     * @return
     */
    @Override
    public VO detail(final Long id, final Integer userId) {
        Optional<DO> optional = this.getRepository().findOne((root, query, cb) ->
                cb.and(cb.equal(root.get("id"), id), cb.equal(root.get("userId"), userId)));
        return this.detail(optional.orElse(null));
    }

    /**
     * 实体转换
     *
     * @param record
     * @return
     */
    protected VO detail(DO record) {
        return this.detail(record, this.getMapper().domainToVo(record));
    }

    protected VO detail(DO record, VO vo) {
        return vo;
    }

    /**
     * 获取列表
     *
     * @param query
     * @param userId
     * @return
     */
    @Override
    public List<VO> list(BaseQuery query, Integer userId) {
        List<DO> list = this.getRepository().findAll(this.spec(query, userId));
        return this.findAll(list);
    }


    protected List<VO> findAll(List<DO> list) {
        return list.stream().map(record -> this.getMapper().domainToVo(record)).toList();
    }

    /**
     * 分页查询
     *
     * @param query
     * @param userId
     * @return
     */
    @Override
    public PageDTO<VO> query(PageQuery query, Integer userId) {
        Page<DO> page = this.getRepository().findAll(this.spec(query, userId),
                PageRequest.of(query.getStartRows(), query.getPageSize()));
        return this.findAll(page);
    }

    protected PageDTO<VO> findAll(Page<DO> page) {
        return PageDTO.toPage(page.getContent().stream().map(record -> this.getMapper().domainToVo(record)).toList(),
                page.getTotalElements(), page.getSize());
    }

    /**
     * 查询条件
     *
     * @param query
     * @param userId
     * @return
     */
    protected Specification<DO> spec(BaseQuery query, Integer userId) {
        return (root, q, cb) -> {
            Predicate user = cb.equal(root.get("userId"), userId);
            if (StrUtil.isNotEmpty(query.getName())) {
                Predicate name = cb.like(root.get("name"), "%" + query.getName() + "%");
                return cb.and(name, user);
            }
            return user;
        };
    }

    @Override
    public void delete(Long id, Integer userId) {

    }

    /**
     * 判断用户
     *
     * @param recordUserId
     * @param userId
     */
    public void assertUser(Integer recordUserId, Integer userId) {
        AssertUtil.isTrue((Objects.equals(recordUserId, userId)),
                ErrorCode.REQUEST_NOT_FOUND.getCode(), ErrorCode.REQUEST_NOT_FOUND.getDesc());
    }
}