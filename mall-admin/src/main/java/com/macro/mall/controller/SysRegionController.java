package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.model.SysRegion;
import com.macro.mall.service.SysRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Tag(name = "SysRegionController", description = "后台区域管理接口")
@RequestMapping("/region")
public class SysRegionController {
    @Autowired
    private SysRegionService regionService;

    @Operation(summary = "添加区域", 
        description = "添加新的区域信息，系统会自动生成区域编码。\n\n" + 
        "【区域编码规则】\n" +
        "- 总长度15位，每个级别占3位\n" +
        "- 未使用的低级别位置用0填充\n" +
        "- 省级：前3位递增，后12位为0\n" +
        "- 市级：使用省级前3位+3位递增码+9位0\n" +
        "- 区级：使用市级前6位+3位递增码+6位0\n" +
        "- 乡镇：使用区级前9位+3位递增码+3位0\n" +
        "- 街道：使用区级前9位+3位递增码+3位0\n\n" +
        "【特殊说明】\n" +
        "- 乡镇级别（第4级）可选，可以从区级直接到街道级\n" +
        "- 父级编码必须存在且级别小于当前级别\n" +
        "- 区域名称在同一级别内不能重复\n" +
        "- status默认为1（启用），sort默认为0\n\n" +
        "【权限要求】\n" +
        "- 需要管理员登录\n" +
        "- 需要区域管理权限")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody SysRegion region) {
        int count = regionService.create(region);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改区域", 
        description = "修改已有区域信息\n\n" +
        "【可修改字段】\n" +
        "- region_name：区域名称\n" +
        "- status：状态（0->禁用；1->启用）\n" +
        "- sort：排序值\n\n" +
        "【不可修改字段】\n" +
        "- region_code：区域编码\n" +
        "- region_level：区域级别\n" +
        "- parent_code：父级编码\n\n" +
        "【权限要求】\n" +
        "- 需要管理员登录\n" +
        "- 需要区域管理权限")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody SysRegion region) {
        int count = regionService.update(id, region);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除区域",
        description = "删除指定区域信息\n\n" +
        "【删除规则】\n" +
        "- 如果存在下级区域，则不允许删除\n" +
        "- 删除后不可恢复\n\n" +
        "【权限要求】\n" +
        "- 需要管理员登录\n" +
        "- 需要区域管理权限")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        int count = regionService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "根据区域编码查询区域信息",
        description = "根据区域编码逐级查询区域信息，从省级开始逐级向下查询。" +
        "\n\n查询规则：" +
        "\n- 输入15位区域编码" +
        "\n- 系统会解析编码中每3位的值" +
        "\n- 只要某一级的3位值不为000，就会包含该级别的信息" +
        "\n- 返回从省级到最后一个非000编码级别的所有区域信息" +
        "\n- 跳过的级别（000）不会包含在返回结果中" +
        "\n- 每个区域信息只会返回一次，避免重复" +
        "\n\n返回字段说明：" +
        "\n- id: 区域ID" +
        "\n- regionCode: 区域编码（15位）" +
        "\n- regionName: 区域名称" +
        "\n- regionLevel: 区域级别(1-5)" +
        "\n- parentCode: 父级区域编码" +
        "\n- status: 状态(0禁用,1启用)" +
        "\n- sort: 排序值" +
        "\n- createTime: 创建时间" +
        "\n- updateTime: 更新时间")
    @ApiResponse(responseCode = "200", description = "查询成功，返回区域信息列表")
    @RequestMapping(value = "/query/{regionCode}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysRegion>> getRegionByCode(
            @Parameter(description = "区域编码，15位数字", required = true, example = "001003004000006")
            @PathVariable String regionCode) {
        List<SysRegion> regionList = regionService.getRegionByCode(regionCode);
        return CommonResult.success(regionList);
    }

    @Operation(summary = "根据地址名称获取地址编号",
        description = "根据完整的地址名称获取最后一级的地址编号。" +
        "\n\n查询规则：" +
        "\n- 支持标准的行政区划格式（省市区县乡镇街道）" +
        "\n- 会自动处理行政区划后缀（省、市、区、县等）" +
        "\n- 返回匹配到的最深层级的区域编码" +
        "\n- 地址可以跨级，如省级直接到街道" +
        "\n\n地址格式说明：" +
        "\n- 标准格式：省级+市级+区县级+乡镇级+街道级" +
        "\n- 必须包含行政区划后缀（省、市、区、县、乡、镇、街道）" +
        "\n- 乡镇级可选，可以省略" +
        "\n- 地址之间不需要特殊分隔符" +
        "\n\n错误处理：" +
        "\n- 地址为空：返回null" +
        "\n- 地址不存在：返回错误信息'未找到对应的地址编号'" +
        "\n- 地址格式错误：返回错误信息'未找到对应的地址编号'")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<String> getRegionCodeByAddress(
            @Parameter(description = "完整地址名称", required = true, example = "浙江省杭州市拱墅区祥福街道")
            @RequestParam String address) {
        String code = regionService.getRegionCodeByAddress(address);
        if (code != null) {
            return CommonResult.success(code);
        }
        return CommonResult.failed("未找到对应的地址编号");
    }

    @Operation(summary = "根据区域级别获取区域列表",
        description = "获取指定级别的所有区域信息。" +
        "\n\n级别说明：" +
        "\n- 1：省级（省、自治区、直辖市）" +
        "\n- 2：市级（地级市、自治州、盟）" +
        "\n- 3：区/县级（市辖区、县级市、县）" +
        "\n- 4：乡镇级（街道办事处、乡、镇）" +
        "\n- 5：街道/社区级" +
        "\n\n返回结果说明：" +
        "\n- 按sort字段升序排序" +
        "\n- sort相同时按id升序排序" +
        "\n- 包含该级别的所有区域完整信息" +
        "\n- status为0表示禁用，1表示启用")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @RequestMapping(value = "/level/{level}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysRegion>> listByLevel(
            @Parameter(description = "区域级别", required = true, example = "1")
            @PathVariable Integer level) {
        List<SysRegion> list = regionService.listByLevel(level);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取下一级区域列表", 
        description = "根据区域编码获取下一级的所有区域信息。" +
        "\n\n查询规则：" +
        "\n- 输入父级区域的15位编码" +
        "\n- 返回该区域的下一级所有区域信息" +
        "\n- 特殊情况：区县级(3级)可以直接查询街道(5级)" +
        "\n- 结果按sort字段升序排序，sort相同则按id升序排序" +
        "\n\n示例：" +
        "\n- 输入省级编码(001000000000000)：返回该省所有市级区域" +
        "\n- 输入市级编码(001001000000000)：返回该市所有区县级区域" +
        "\n- 输入区县级编码(001001001000000)：返回该区县下属街道/社区")
    @RequestMapping(value = "/nextLevel/{regionCode}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysRegion>> listNextLevel(
            @Parameter(description = "区域编码，15位数字", required = true, example = "001000000000000")
            @PathVariable String regionCode) {
        List<SysRegion> regionList = regionService.listNextLevel(regionCode);
        return CommonResult.success(regionList);
    }
} 