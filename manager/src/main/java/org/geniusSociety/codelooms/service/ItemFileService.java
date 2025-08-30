/**
 * Cealus Li 2025/7/26
 * Copyright
 */
package org.geniusSociety.codelooms.service;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.persistence.criteria.Predicate;
import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.common.base.BaseService;
import org.geniusSociety.codelooms.common.bo.BaseQuery;
import org.geniusSociety.codelooms.common.constant.BaseConstant;
import org.geniusSociety.codelooms.dao.CvItemFileRepository;
import org.geniusSociety.codelooms.domain.entity.CvItemFile;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.geniusSociety.codelooms.mapper.ItemFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

/**
 * 项目文件
 *
 * @author Cealus Li 2025/7/26
 */
@Service
public class ItemFileService extends BaseService<FileVO, CvItemFile> {

    @Autowired
    private ItemFileMapper itemFileMapper;
    @Autowired
    private CvItemFileRepository itemFileRepository;

    @Override
    protected FileVO detail(CvItemFile record, FileVO vo) {
        File file;
        if (StrUtil.isNotEmpty(record.getSpPath()) && (file = FileUtil.file(record.getSpPath())).exists()) {
            String sp = FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8);
            vo.setSp(sp);
        }
        if (StrUtil.isNotEmpty(record.getSqlPath()) && (file = FileUtil.file(record.getSqlPath())).exists()) {
            String sql = FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8);
            vo.setSql(sql);
        }
        return vo;
    }


    @Override
    protected Specification<CvItemFile> spec(BaseQuery query, Integer userId) {
        return (root, q, cb) -> {
            Predicate and = cb.and(cb.equal(root.get("itemId"), query.getId())
                    , cb.equal(root.get("isDel"), BaseConstant.NO)
                    , cb.equal(root.get("userId"), userId));
            if (StrUtil.isNotEmpty(query.getName())) {
                Predicate name = cb.like(root.get("name"), "%" + query.getName() + "%");
                return cb.and(and, name);
            }
            return and;
        };
    }

    @Override
    public void delete(Long id, Integer userId) {
        Optional<CvItemFile> optional = itemFileRepository.findById(id);
        if (optional.isPresent()) {
            CvItemFile file = optional.get();
            this.assertUser(file.getUserId(), userId);
            file.setIsDel(BaseConstant.YES);
            itemFileRepository.save(file);
        }
    }

    @Override
    protected JpaSpecificationExecutor<CvItemFile> getRepository() {
        return itemFileRepository;
    }

    @Override
    protected BaseMapper<FileVO, CvItemFile> getMapper() {
        return itemFileMapper;
    }
}
