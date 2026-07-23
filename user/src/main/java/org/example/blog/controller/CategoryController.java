package org.example.blog.controller;

import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CategoryResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Category;
import org.example.blog.service.CategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/list")
    public Result<PageResponse<CategoryResponse>> pageCategory(@RequestBody PageRequest request){
        return Result.success(categoryService.pageCategory(request));
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id){
        return Result.success(categoryService.getCategoryById(id));
    }
}
