package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.TagRequest;
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

    @PostMapping
    public Result<String> createTag(@Valid @RequestBody TagRequest tagRequest){
        tagService.createTag(tagRequest.getTagName());
        return Result.success("标签创建成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@PathVariable Long id){
        tagService.deleteTagById(id);
        return Result.success("标签删除成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateTag(@PathVariable Long id,
                                    @Valid @RequestBody TagRequest tagRequest){
        tagService.updateTagById(id,tagRequest.getTagName());
        return Result.success("标签修改成功");
    }

    @GetMapping
    public Result<PageResponse<TagResponse>> pageTag(@RequestParam(defaultValue = "1")Long page,
                                                     @RequestParam(defaultValue = "10")Long size){
        return Result.success(tagService.getAllTag(page,size));
    }

    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable Long id){
        return Result.success(tagService.getTagById(id));
    }
}
