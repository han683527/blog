package org.example.blog.controller;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.dto.response.TagResponse;
import org.example.blog.entity.Tag;
import org.example.blog.service.TagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/list")
    public Result<PageResponse<TagResponse>> pageTag(@RequestBody PageRequest request){
        return Result.success(tagService.pageTag(request));
    }

    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable Long id){
        return Result.success(tagService.getTagById(id));
    }
}
