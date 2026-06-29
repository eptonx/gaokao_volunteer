package com.example.gaokaoproject.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataImportLog {
    private Long id;
    private String targetTable;
    private String sourceFileName;
    private Integer totalRows;
    private Integer successRows;
    private Integer failRows;
    private Integer status;
    private String errorMessage;
    private Long operatorId;
    private LocalDateTime createdAt;
}