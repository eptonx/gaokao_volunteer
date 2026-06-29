package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.Attachment;
import java.util.List;

public interface AttachmentService {
    List<Attachment> selectAll();
    Attachment selectById(Long id);
    Integer update(Attachment attachment);
    Integer add(Attachment attachment);
    Integer delete(Long id);
}