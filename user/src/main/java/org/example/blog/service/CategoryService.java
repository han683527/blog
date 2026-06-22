package org.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blog.dto.response.CategoryResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.entity.Category;


public interface CategoryService extends IService<Category> {
    void createCategory(String categoryName);

    void deleteCategory(Long id);

    void updateCategory(String categoryName,Long categoryId);

    PageResponse<CategoryResponse> getAllCategory(int page, int size);

    Category getCategoryById(Long categoryId);
}
