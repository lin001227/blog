package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageRankingRequest {

    @NotNull(message = "排名顺序不能为空")
    private Integer rankOrder;

    @NotBlank(message = "语言名称不能为空")
    private String languageName;

    @NotNull(message = "百分比不能为空")
    private Double percentage;

    private String trend = "stable";

    private String color;

    private String iconUrl;

    private String description;

    private String month;
}
