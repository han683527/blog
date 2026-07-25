package org.example.blog.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.CategoryRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CategoryResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.service.CategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<String> createCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        categoryService.createCategory(categoryRequest);
        return Result.success("种类创建成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return Result.success("种类删除成功");
    }

    @PutMapping
    public Result<String> updateCategory(@Valid @RequestBody CategoryRequest request){
        categoryService.updateCategory(request);
        return Result.success("种类修改成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<CategoryResponse>> adminPageCategory(@Valid @RequestBody PageRequest request){
        return Result.success(categoryService.adminPageCategory(request));
    }
}
