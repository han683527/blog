package org.example.blog.dto.response;

import lombok.Data;

import java.util.List;

// 对比 IPage 返回 records、total、pages、current、size 所有的信息; PageResponse 自定义接口可以自行决定要展示的信息
@Data
public class PageResponse<T> {

    private long total;

    private List<T> list;
}
