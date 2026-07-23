package org.example.blog.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.request.TagRequest;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.TagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @PostMapping
    public Result<String> createTag(@Valid @RequestBody TagRequest request) {
        tagService.createTag(request);
        return Result.success("标签创建成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTagById(id);
        return Result.success("标签删除成功");
    }

    @PutMapping
    public Result<String> updateTag(@Valid @RequestBody TagRequest request) {
        tagService.updateTagById(request);
        return Result.success("标签修改成功");
    }

    @PostMapping("/list")
    public Result<PageResponse> adminPageTag(@Valid @RequestBody PageRequest request){
        return Result.success(tagService.adminPageTag(request));
    }
}
