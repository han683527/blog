package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.request.CategoryRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CategoryResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Category;


public interface CategoryService extends IService<Category> {
    void createCategory(CategoryRequest request);

    void deleteCategory(Long id);

    void updateCategory(CategoryRequest request);

    PageResponse<CategoryResponse> adminPageCategory(PageRequest request);
    // 用户分页查询
    PageResponse<CategoryResponse> pageCategory(PageRequest request);

    Category getCategoryById(Long categoryId);
}
