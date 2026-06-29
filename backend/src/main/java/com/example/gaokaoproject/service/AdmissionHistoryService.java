package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.AdmissionHistory;
import java.util.List;

public interface AdmissionHistoryService {
    /**
     * 查询所有录取历史
     * @return 录取历史列表
     */
    List<AdmissionHistory> selectAll();
    
    /**
     * 根据ID查询录取历史
     * @param id 录取历史ID
     * @return 录取历史信息
     */
    AdmissionHistory selectById(Long id);
    
    /**
     * 根据院校ID查询录取历史
     * @param institutionId 院校ID
     * @return 录取历史列表
     */
    List<AdmissionHistory> selectByInstitutionId(Long institutionId);
    
    /**
     * 添加录取历史
     * @param history 录取历史信息
     * @return 操作结果
     */
    boolean add(AdmissionHistory history);
    
    /**
     * 更新录取历史
     * @param history 录取历史信息
     * @return 操作结果
     */
    boolean update(AdmissionHistory history);
    
    /**
     * 删除录取历史
     * @param id 录取历史ID
     * @return 操作结果
     */
    boolean delete(Long id);
    boolean publish(Long id, Integer status);

    List<AdmissionHistory> selectPage(Long institutionId, int pageNum, int pageSize);
}
