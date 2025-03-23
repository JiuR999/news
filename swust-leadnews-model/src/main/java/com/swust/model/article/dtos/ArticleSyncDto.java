package com.swust.model.article.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleSyncDto {
    private Long id;
    private Long value;
    private String column;
}
