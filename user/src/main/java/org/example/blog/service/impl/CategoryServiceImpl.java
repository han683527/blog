package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.request.CategoryRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CategoryResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Category;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.CategoryMapper;
import org.example.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void createCategory(CategoryRequest request){
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        this.save(category);
    }

    @Override
    public void deleteCategory(Long categoryId){
        Category category = this.getOptById(categoryId)
                .orElseThrow(() -> new NotFoundException("文章种类不存在"));
        this.removeById(categoryId);

        redisTemplate.delete("category:" + categoryId);
        log.info("删除缓存: category: {}",category);
    }

    @Override
    public void updateCategory(CategoryRequest request){
        Long categoryId = request.getCategoryId();
        Category category = this.getOptById(categoryId)
                .orElseThrow(() -> new NotFoundException("文章种类不存在"));
        category.setCategoryName(request.getCategoryName());
        category.setId(categoryId);
        this.updateById(category);

        redisTemplate.delete("category:" + categoryId);
        log.info("删除缓存: category: {}",category);
    }

    @Override
    public PageResponse<CategoryResponse> pageCategory(PageRequest request){
        Page<Category> p = this.page(new Page<>(request.getPage(),request.getSize()));
        List<CategoryResponse> list = BeanUtil.copyToList(p.getRecords(), CategoryResponse.class);
        PageResponse<CategoryResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    @Override
    public Category getCategoryById(Long categoryId){
        String key = "category:" + categoryId;
        String cached = redisTemplate.opsForValue().get(key);
        if(cached!=null){
            log.info("缓存命中: {}",key);
            if("NULL".equals(cached)){
                throw new NotFoundException("文章种类不存在");
            }
            return JSONUtil.toBean(cached,Category.class);
        }

        Category category = this.getById(categoryId);
        if(category==null){
            redisTemplate.opsForValue().set(key,"NULL",1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}",key);
            throw new NotFoundException("文章种类不存在");
        }

        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(category),10, TimeUnit.MINUTES);
        log.info("写入缓存: {}",key);
        return category;
    }
}
