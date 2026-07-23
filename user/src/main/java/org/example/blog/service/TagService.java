package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.request.TagRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.TagResponse;
import org.example.blog.entity.Tag;

public interface TagService extends IService<Tag> {

    void createTag(TagRequest request);

    void deleteTagById(Long id);

    void updateTagById(TagRequest request);

    PageResponse<TagResponse> adminPageTag(PageRequest request);

    PageResponse<TagResponse> pageTag(PageRequest request);

    Tag getTagById(Long id);
}
