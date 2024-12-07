package com.macro.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysRegion implements Serializable {
    private Long id;

    @Schema(description = "区域编码")
    private String regionCode;

    @Schema(description = "区域名称")
    private String regionName;

    @Schema(description = "区域级别：1->省；2->市；3->区/县；4->乡镇；5->街道/社区")
    private Integer regionLevel;

    @Schema(description = "父级区域编码")
    private String parentCode;

    @Schema(description = "状态：0->禁用；1->启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
} 