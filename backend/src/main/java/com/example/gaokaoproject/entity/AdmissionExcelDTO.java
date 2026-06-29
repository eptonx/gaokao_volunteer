package com.example.gaokaoproject.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 导入模板对应的 DTO，列名与 Excel 表头一一对应。
 */
@Data
public class AdmissionExcelDTO {

    @ExcelProperty("院校ID")
    private Long institutionId;

    @ExcelProperty("专业ID")
    private Long majorId;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("年份")
    private Integer year;

    @ExcelProperty("省份代码")
    private String provinceCode;

    @ExcelProperty("科类代码")
    private String categoryCode;

    @ExcelProperty("最低分")
    private Integer minScore;

    @ExcelProperty("最高分")
    private Integer maxScore;

    @ExcelProperty("平均分")
    private Integer avgScore;

    @ExcelProperty("最低位次")
    private Integer minRank;

    @ExcelProperty("录取人数")
    private Integer enrollmentCount;
}
