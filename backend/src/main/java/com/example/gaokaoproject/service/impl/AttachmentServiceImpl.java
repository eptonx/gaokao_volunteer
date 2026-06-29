package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.Attachment;
import com.example.gaokaoproject.mapper.AttachmentMapper;
import com.example.gaokaoproject.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Override
    public List<Attachment> selectAll() {
        return attachmentMapper.selectAll();
    }

    @Override
    public Attachment selectById(Long id) {
        return attachmentMapper.selectById(id);
    }

    @Override
    public Integer update(Attachment attachment) {
        return attachmentMapper.update(attachment);
    }

    @Override
    public Integer add(Attachment attachment) {
        return attachmentMapper.add(attachment);
    }

    @Override
    public Integer delete(Long id) {
        return attachmentMapper.delete(id);
    }
}