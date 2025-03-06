package com.swust.model.common.pojos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CurrentUser implements Serializable {
    private Integer id;
    private String name;
}
