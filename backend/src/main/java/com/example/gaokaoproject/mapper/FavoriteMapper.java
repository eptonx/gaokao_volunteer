package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    // 添加收藏
    @Insert("insert into favorite(user_id, type, target_id, enrollment_plan_id, created_at) values(#{userId}, #{type}, #{targetId}, #{enrollmentPlanId}, #{createdAt})")
    Integer add(Favorite favorite);
    
    // 更新收藏
    @Update("update favorite set user_id = #{userId}, type = #{type}, target_id = #{targetId}, enrollment_plan_id = #{enrollmentPlanId}, created_at = #{createdAt} where id = #{id}")
    Integer update(Favorite favorite);
    
    // 根据ID删除收藏
    @Delete("delete from favorite where id = #{id}")
    Integer delete(Long id);
    
    // 根据用户ID和目标ID删除收藏
    @Delete("delete from favorite where user_id = #{userId} and target_id = #{targetId} and type = #{type}")
    Integer deleteByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("type") Integer type);
    
    // 查询所有收藏
    @Select("select id, user_id, type, target_id, enrollment_plan_id, created_at from favorite")
    List<Favorite> selectAll();
    
    // 根据ID查询收藏
    @Select("select id, user_id, type, target_id, enrollment_plan_id, created_at from favorite where id = #{id}")
    Favorite selectById(Long id);
    
    // 根据用户ID查询收藏
    @Select("select id, user_id, type, target_id, enrollment_plan_id, created_at from favorite where user_id = #{userId} and type = #{type}")
    List<Favorite> selectByUserId(@Param("userId") Long userId, @Param("type") Integer type);

    // 根据用户ID查询所有收藏（不区分类型）
    @Select("select id, user_id, type, target_id, enrollment_plan_id, created_at from favorite where user_id = #{userId}")
    List<Favorite> selectAllByUserId(@Param("userId") Long userId);
    
    // 检查是否已收藏
    @Select("select count(*) from favorite where user_id = #{userId} and target_id = #{targetId} and type = #{type}")
    Integer countByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("type") Integer type);
}
