package com.xlzfa.knowhub.domain.pojo;

import java.io.Serializable;

/**
 * (Tag)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:22:36
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 379560650502600029L;

    private Integer id;

    private String name;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

