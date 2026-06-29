package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Institution;
import java.util.List;
import java.util.Map;

/**
 * 考生端院校服务接口
 */
public interface StudentInstitutionService {

    /**
     * 多条件筛选查询院校列表
     * @param params 筛选条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 院校列表
     */
    List<Institution> selectByConditions(Map<String, Object> params, Integer pageNum, Integer pageSize);

    /**
     * 关键词搜索院校
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 院校列表
     */
    List<Institution> searchByKeyword(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 查询院校筛选项字典
     * @return 筛选项字典
     */
    Map<String, List<String>> getFilterOptions();

    /**
     * 多条件筛选查询总数
     */
    Long countByConditions(Map<String, Object> params);

    /**
     * 关键词搜索总数
     */
    Long countByKeyword(String keyword);
}
