package com.example.gaokaoproject.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.example.gaokaoproject.entity.AdmissionExcelDTO;
import com.example.gaokaoproject.entity.AdmissionHistory;
import com.example.gaokaoproject.mapper.AdmissionHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * EasyExcel 读取监听器 — 每100条批量插入数据库。
 * 注意：必须加 @Component，否则 Controller 无法通过 @Autowired 注入。
 */
@Component
public class AdmissionExcelListener implements ReadListener<AdmissionExcelDTO> {
//监听器
    private static final int BATCH_COUNT = 100;
    //每一百条数据批量注入
    private List<AdmissionHistory> cachedList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    //缓存列表
    @Autowired
    private AdmissionHistoryMapper admissionHistoryMapper;

    @Override
    //① 数据转换：DTO → Entity
    //Excel读取 → 每行触发invoke() → 转换对象 → 加入缓存 → 判断是否满100条 → 满则批量插入
    public void invoke(AdmissionExcelDTO data, AnalysisContext context) {
        // 把 Excel 行转成数据库实体
        AdmissionHistory entity = new AdmissionHistory();
        entity.setInstitutionId(data.getInstitutionId());
        entity.setMajorId(data.getMajorId());
        entity.setMajorName(data.getMajorName());
        entity.setYear(data.getYear());
        entity.setProvinceCode(data.getProvinceCode());
        entity.setCategoryCode(data.getCategoryCode());
        entity.setMinScore(data.getMinScore());
        entity.setMaxScore(data.getMaxScore());
        entity.setAvgScore(data.getAvgScore());
        entity.setMinRank(data.getMinRank());
        entity.setEnrollmentCount(data.getEnrollmentCount());

        cachedList.add(entity);

        // 达到 BATCH_COUNT 条就批量存一次
        if (cachedList.size() >= BATCH_COUNT) {
            saveData();
            cachedList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 读完所有行后，把剩余的数据也存进去
        saveData();
    }

    private void saveData() {
        if (cachedList.isEmpty()) {
            return;
        }
        for (AdmissionHistory entity : cachedList) {
            admissionHistoryMapper.insert(entity);
        }
        System.out.println("成功导入 " + cachedList.size() + " 条数据");
    }
}
