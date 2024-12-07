package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.SysRegion;
import com.macro.mall.portal.service.SysRegionPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台区域管理Controller
 */
@Controller
@Tag(name = "SysRegionPortalController", description = "前台区域管理接口")
@RequestMapping("/region")
public class SysRegionPortalController {
    @Autowired
    private SysRegionPortalService regionPortalService;

    @Operation(summary = "根据区域编码查询区域信息",
        description = "根据区域编码逐级查询区域信息\n\n" +
        "【查询规则】\n" +
        "- 需要登录后访问\n" +
        "- 输入15位区域编码\n" +
        "- 系统会解析编码中每3位的值\n" +
        "- 只要某一级的3位值不为000，就会包含该级别的信息\n" +
        "- 返回从省级到最后一个非000编码级别的所有区域信息\n" +
        "- 跳过的级别（000）不会包含在返回结果中\n" +
        "- 每个区域信息只会返回一次，避免重复\n\n" +
        "【返回字段说明】\n" +
        "- id：区域ID\n" +
        "- regionCode：区域编码（15位）\n" +
        "- regionName：区域名称\n" +
        "- regionLevel：区域级别\n" +
        "- parentCode：父级编码\n" +
        "- status：状态（0->禁用；1->启用）\n" +
        "- sort：排序值")
    @RequestMapping(value = "/query/{regionCode}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysRegion>> getRegionByCode(
            @Parameter(description = "区域编码，15位数字", required = true, example = "001003004000006")
            @PathVariable String regionCode) {
        List<SysRegion> regionList = regionPortalService.getRegionByCode(regionCode);
        return CommonResult.success(regionList);
    }

    @Operation(summary = "根据地址名称获取区域编码",
        description = "根据地址名称解析获取对应的区域编码\n\n" +
        "【功能说明】\n" +
        "- 支持省市区街道等多级地址\n" +
        "- 地址之间用空格分隔\n" +
        "- 返回最精确的匹配结果\n\n" +
        "【示例输入】\n" +
        "- 浙江省 杭州市 西湖区\n" +
        "- 浙江省杭州市西湖区\n" +
        "- 杭州市西湖区\n\n" +
        "【注意事项】\n" +
        "- 需要登录后访问\n" +
        "- 地址必须是系统中存在的")
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<String> getRegionCodeByAddress(
            @Parameter(description = "地址名称", required = true, example = "浙江省杭州市西湖区")
            @RequestParam String address) {
        String regionCode = regionPortalService.getRegionCodeByAddress(address);
        return CommonResult.success(regionCode);
    }

    @Operation(summary = "获取下一级区域列表", 
        description = "根据区域编码获取下一级的所有区域信息\n\n" +
        "【查询规则】\n" +
        "- 需要登录后访问\n" +
        "- 输入父级区域的15位编码\n" +
        "- 返回该区域的下一级所有区域信息\n" +
        "- 特殊情况：区县级(3级)可以直接查询街道(5级)\n" +
        "- 结果按sort字段升序排序，sort相同则按id升序排序\n\n" +
        "【示例说明】\n" +
        "- 输入省级编码(001000000000000)：返回该省所有市级区域\n" +
        "- 输入市级编码(001001000000000)：返回该市所有区县级区域\n" +
        "- 输入区县级编码(001001001000000)：返回该区县下属街道/社区\n\n" +
        "【级别说明】\n" +
        "- 1级：省、自治区、直辖市\n" +
        "- 2级：地级市、自治州、盟\n" +
        "- 3级：市辖区、县级市、县\n" +
        "- 4级：街道办事处、乡、镇\n" +
        "- 5级：社区、村")
    @RequestMapping(value = "/nextLevel/{regionCode}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysRegion>> listNextLevel(
            @Parameter(description = "区域编码，15位数字", required = true, example = "001000000000000")
            @PathVariable String regionCode) {
        List<SysRegion> regionList = regionPortalService.listNextLevel(regionCode);
        return CommonResult.success(regionList);
    }
} 