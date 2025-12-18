package com.xlzfa.knowhub.domain.vo;

import java.util.List;

public class PageVo<T> {

    private List<T> rows;      // 当前页数据
    private Long total;        // 总条数

    public PageVo() {}

    public PageVo(List<T> rows, Long total) {
        this.rows = rows;
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
