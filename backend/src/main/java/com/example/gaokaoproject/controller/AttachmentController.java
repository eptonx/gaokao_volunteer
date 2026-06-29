package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.Attachment;
import com.example.gaokaoproject.service.AttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/attachment")
@Tag(name = "附件上传端")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping("/selectAll")
    public List<Attachment> selectAll() {
        return attachmentService.selectAll();
    }

    @GetMapping("/getOne")
    public Attachment getOne(@RequestParam Long id) {
        return attachmentService.selectById(id);
    }

    @PostMapping("/add")
    public Integer add(@RequestBody Attachment attachment) {
        return attachmentService.add(attachment);
    }

    @PutMapping("/update")
    public Integer update(@RequestBody Attachment attachment) {
        return attachmentService.update(attachment);
    }

    @DeleteMapping("/delete")
    public Integer delete(@RequestParam Long id) {
        return attachmentService.delete(id);
    }
}