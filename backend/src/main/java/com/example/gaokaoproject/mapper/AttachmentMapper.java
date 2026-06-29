package com.example.gaokaoproject.mapper;
import com.example.gaokaoproject.entity.Attachment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AttachmentMapper {
    @Select("select id,biz_type,biz_id,file_name,file_url,file_size,file_ext,upload_by,created_at from attachment")
    List<Attachment> selectAll();

    @Select("select id,biz_type,biz_id,file_name,file_url,file_size,file_ext,upload_by,created_at from attachment where id = #{id}")
    Attachment selectById(Long id);

    @Update("update attachment set biz_type=#{bizType},biz_id=#{bizId},file_name=#{fileName},file_url=#{fileUrl},file_size=#{fileSize},file_ext=#{fileExt},upload_by=#{uploadBy} where id=#{id}")
    Integer update(Attachment attachment);

    @Insert("insert into attachment(biz_type,biz_id,file_name,file_url,file_size,file_ext,upload_by,created_at) values(#{bizType},#{bizId},#{fileName},#{fileUrl},#{fileSize},#{fileExt},#{uploadBy},NOW())")
    Integer add(Attachment attachment);

    @Delete("delete from attachment where id = #{id}")
    Integer delete(Long id);
}