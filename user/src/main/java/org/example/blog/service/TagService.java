package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.TagResponse;
import org.example.blog.entity.Tag;

public interface TagService extends IService<Tag> {

    void createTag(String tagName);

    void deleteTagById(Long id);

    void updateTagById(Long id, String tagName);

    PageResponse<TagResponse> getAllTag(Long page,Long size);

    Tag getTagById(Long id);
}
