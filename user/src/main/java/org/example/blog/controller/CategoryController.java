package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.CategoryRequest;
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

    @PostMapping
    public Result<String> createCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        categoryService.createCategory(categoryRequest.getCategoryName());
        return Result.success("种类创建成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return Result.success("种类删除成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateCategory(@PathVariable Long id,
                                         @Valid @RequestBody CategoryRequest categoryRequest){
        categoryService.updateCategory(categoryRequest.getCategoryName(),id);
        return Result.success("种类修改成功");
    }

    @GetMapping
    public Result<PageResponse<CategoryResponse>> getAllCategory(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        return Result.success(categoryService.getAllCategory(page,size));
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id){
        return Result.success(categoryService.getCategoryById(id));
    }
}
