package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.ScoreRank;
import org.apache.ibatis.annotations.*;

/**
 * 一分一段表 Mapper
 */
@Mapper
public interface ScoreRankMapper {

    /**
     * 正向查位次：根据 (年份, 科类, 分数, 省份) 查累计人数（位次）
     * 分数可能落在 score_min ~ score_max 区间内
     */
    @Select("SELECT * FROM score_rank " +
            "WHERE year = #{year} AND subject_type = #{subjectType} " +
            "AND score_min <= #{score} AND score_max >= #{score} " +
            "AND province_code = #{provinceCode} " +
            "LIMIT 1")
    ScoreRank findByScore(@Param("year") int year,
                          @Param("subjectType") String subjectType,
                          @Param("score") int score,
                          @Param("provinceCode") String provinceCode);

    /**
     * 反向查分数：根据 (年份, 科类, 位次, 省份) 查对应分数段
     * 取累计人数 >= 给定位次 的最小分数段（即该位次对应的分数）
     */
    @Select("SELECT * FROM score_rank " +
            "WHERE year = #{year} AND subject_type = #{subjectType} " +
            "AND cumulative_count >= #{rank} " +
            "AND province_code = #{provinceCode} " +
            "ORDER BY cumulative_count ASC LIMIT 1")
    ScoreRank findByRank(@Param("year") int year,
                         @Param("subjectType") String subjectType,
                         @Param("rank") int rank,
                         @Param("provinceCode") String provinceCode);

    /**
     * 查询某年某科类的最高分（用于验证位次是否合理）
     */
    @Select("SELECT cumulative_count FROM score_rank " +
            "WHERE year = #{year} AND subject_type = #{subjectType} " +
            "AND province_code = #{provinceCode} " +
            "ORDER BY cumulative_count DESC LIMIT 1")
    Integer getMaxRank(@Param("year") int year,
                       @Param("subjectType") String subjectType,
                       @Param("provinceCode") String provinceCode);
}
