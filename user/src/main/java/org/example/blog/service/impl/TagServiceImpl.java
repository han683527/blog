package org.example.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.TagResponse;
import org.example.blog.entity.Tag;
import org.example.blog.exception.NotFoundException;
import org.example.blog.mapper.TagMapper;
import org.example.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void createTag(String tagName){
        Tag tag = new Tag();
        tag.setTagName(tagName);
        this.save(tag);
    }

    @Override
    public void deleteTagById(Long id){
        Tag tag = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("标签不存在"));
        this.removeById(id);

        redisTemplate.delete("tag:" + id);
        log.info("删除缓存: tag: {}",tag);
    }

    @Override
    public void updateTagById(Long id,String tagName){
        Tag tag = this.getOptById(id)
                .orElseThrow(() -> new NotFoundException("标签不存在"));
        tag.setTagName(tagName);
        this.updateById(tag);

        redisTemplate.delete("tag:" + id);
        log.info("删除缓存: tag: {}",tag);
    }

    @Override
    public PageResponse<TagResponse> getAllTag(Long page, Long size){
        Page<Tag> p = this.page(new Page<>(page,size));
        List<TagResponse> list = BeanUtil.copyToList(p.getRecords(), TagResponse.class);
        PageResponse<TagResponse> response = new PageResponse<>();
        response.setTotal(p.getTotal());
        response.setList(list);
        return response;
    }

    @Override
    public Tag getTagById(Long id){
        String key = "tag:" + id;
        String cached = redisTemplate.opsForValue().get(key);
        if(cached!=null){
            log.info("缓存命中: {}",key);
            if("NULL".equals(cached)){
                throw new NotFoundException("标签不存在");
            }
            return JSONUtil.toBean(cached,Tag.class);
        }

        Tag tag = this.getById(id);
        if(tag==null){
            redisTemplate.opsForValue().set(key,"NULL",1, TimeUnit.MINUTES);
            log.info("写入空值缓存(防穿透): {}",key);
            throw new NotFoundException("标签不存在");
        }

        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(tag),10, TimeUnit.MINUTES);
        log.info("写入缓存: {}",key);
        return tag;
    }

}
