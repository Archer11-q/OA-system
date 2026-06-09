package com.oasystem.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装
 * <p>
 * 所有分页查询的 Controller 返回值使用此类包装。
 *
 * @param <T> 数据项类型
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    /** 当前页码（从1开始） */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private long totalPages;

    /** 数据列表 */
    private List<T> records;

    public PageResult(long pageNum, long pageSize, long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (total + pageSize - 1) / pageSize;
        this.records = records != null ? records : Collections.emptyList();
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(1, 10, 0, Collections.emptyList());
    }
}
