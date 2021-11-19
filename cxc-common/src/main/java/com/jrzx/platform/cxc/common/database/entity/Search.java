package com.jrzx.platform.cxc.common.database.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 搜索封装类
 */
@Data
@ApiModel(description = "搜索条件")
public class Search implements Serializable {

	@ApiModelProperty(value = "关键词")
	private String keyword;

	@ApiModelProperty(value = "开始日期")
	private String startDate;

	@ApiModelProperty(value = "结束日期")
	private String endDate;
	@ApiModelProperty(value = "数据范围")
	private String dataScope;
	@ApiModelProperty(value = "分页大小")
	private int size=10;
	@ApiModelProperty(value = "页码")
	private int page;
}
