package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Institution;
import com.example.gaokaoproject.mapper.InstitutionMapper;
import com.example.gaokaoproject.service.InstitutionService;
import com.example.gaokaoproject.service.StudentInstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Service
public class InstitutionServiceImpl implements InstitutionService {

    @Autowired
    private InstitutionMapper institutionMapper;

    @Override
    public Integer add(Institution institution) {
        if (institution.getCreatedAt() == null) {
            institution.setCreatedAt(LocalDateTime.now());
        }
        if (institution.getUpdatedAt() == null) {
            institution.setUpdatedAt(LocalDateTime.now());
        }
        return institutionMapper.add(institution);
    }

    @Override
    public Integer update(Institution institution) {
        institution.setUpdatedAt(LocalDateTime.now());
        return institutionMapper.update(institution);
    }

    @Override
    public Integer delete(Long id) {
        return institutionMapper.delete(id);
    }

    @Override
    public List<Institution> selectAll() {
        return institutionMapper.selectAll();
    }

    @Override
    public Institution selectById(Long id) {
        return institutionMapper.selectById(id);
    }

    @Override
    public Institution selectByName(String name) {
        return institutionMapper.selectByName(name);
    }

    /**
     * 多条件筛选查询院校列表
     * @param params 筛选条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 院校列表
     */
    public List<Institution> selectByConditions(Map<String, Object> params, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return institutionMapper.selectByConditions(params, offset, pageSize);
    }

    /**
     * 关键词搜索院校
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 院校列表
     */
    public List<Institution> searchByKeyword(String keyword, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return institutionMapper.searchByKeyword(keyword, offset, pageSize);
    }

    /**
     * 查询院校筛选项字典
     * @return 筛选项字典
     */
    public Map<String, List<String>> getFilterOptions() {
        // 简单实现，实际应从数据库获取
        Map<String, List<String>> options = new HashMap<>();

        // 省份选项
        List<String> provinces = Arrays.asList("北京市", "天津市", "河北省", "山西省", "内蒙古自治区",
                "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省", "浙江省", "安徽省",
                "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省",
                "广西壮族自治区", "海南省", "重庆市", "四川省", "贵州省", "云南省",
                "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区");
        options.put("province", provinces);

        // 层次选项
        List<String> levels = Arrays.asList("本科", "专科", "本科专科", "研究生");
        options.put("level", levels);

        // 性质选项
        List<String> properties = Arrays.asList("公办", "民办", "独立学院", "中外合作");
        options.put("property", properties);

        // 科类选项
        List<String> subjects = Arrays.asList("文科", "理科", "综合", "艺术", "体育");
        options.put("subject", subjects);

        return options;
    }

    @Override
    public Institution updateSelfFields(Institution institution) {
        // 有则更新，无则自动创建占位记录
        Long id = institution.getId();
        if (id != null) {
            Institution exist = institutionMapper.selectById(id);
            if (exist != null) {
                // 更新时：空值沿用库中已有值，防止覆盖
                if (institution.getInstitutionCode() == null || institution.getInstitutionCode().isEmpty()) {
                    institution.setInstitutionCode(exist.getInstitutionCode());
                }
                if (institution.getInstitutionName() == null || institution.getInstitutionName().isEmpty()) {
                    institution.setInstitutionName(exist.getInstitutionName());
                }
                institutionMapper.updateSelfFields(institution);
                return institution;
            }
        }
        // 自动创建：优先使用用户提交的名称和代码
        if (institution.getInstitutionCode() == null || institution.getInstitutionCode().isEmpty()) {
            institution.setInstitutionCode(id != null ? "SCH" + id : "SCH_AUTO");
        }
        if (institution.getInstitutionName() == null || institution.getInstitutionName().isEmpty()) {
            institution.setInstitutionName(id != null ? "院校" + id : "新院校");
        }
        if (institution.getInstitutionType() == null) institution.setInstitutionType(1);
        if (institution.getProvinceCode() == null) institution.setProvinceCode("11");
        if (institution.getCityCode() == null) institution.setCityCode("1101");
        if (institution.getLevel() == null) institution.setLevel("1");
        if (institution.getNature() == null) institution.setNature(1);
        if (institution.getIsOnline() == null) institution.setIsOnline(0);
        if (institution.getStatus() == null) institution.setStatus(1);
        institution.setCreatedAt(LocalDateTime.now());
        institution.setUpdatedAt(LocalDateTime.now());
        if (id != null && id > 0) {
            institutionMapper.addSelf(institution);
        } else {
            institutionMapper.addSelfAuto(institution); // 由数据库自动生成ID
        }
        return institution;
    }

    // ==================== 运营端管控方法 ====================

    @Override
    public Integer updateOnlineStatus(Long id, Integer isOnline) {
        return institutionMapper.updateOnlineStatus(id, isOnline);
    }

    @Override
    public Integer updateLevel(Long id, String level) {
        return institutionMapper.updateLevel(id, level);
    }

    @Override
    public Integer updateStatus(Long id, Integer status) {
        return institutionMapper.updateStatus(id, status);
    }
}
