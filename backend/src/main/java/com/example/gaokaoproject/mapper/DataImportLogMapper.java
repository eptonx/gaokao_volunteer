package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.DataImportLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DataImportLogMapper {

    @Select("select * from data_import_log")
    List<DataImportLog> selectAll();

    @Select("select * from data_import_log where id = #{id}")
    DataImportLog selectById(Long id);

    @Insert("insert into data_import_log (target_table, source_file_name, total_rows, success_rows, fail_rows, " +
            "status, error_message, operator_id, created_at) values (#{targetTable}, #{sourceFileName}, " +
            "#{totalRows}, #{successRows}, #{failRows}, #{status}, #{errorMessage}, #{operatorId}, #{createdAt})")
    int add(DataImportLog log);

    @Update("update data_import_log set target_table = #{targetTable}, source_file_name = #{sourceFileName}, " +
            "total_rows = #{totalRows}, success_rows = #{successRows}, fail_rows = #{failRows}, " +
            "status = #{status}, error_message = #{errorMessage}, operator_id = #{operatorId}, " +
            "created_at = #{createdAt} where id = #{id}")
    int update(DataImportLog log);

    @Delete("delete from data_import_log where id = #{id}")
    int delete(Long id);
}