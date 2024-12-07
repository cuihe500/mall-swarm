package com.macro.mall.service.impl;

import com.macro.mall.common.api.ResultCode;
import com.macro.mall.mapper.SysRegionMapper;
import com.macro.mall.model.SysRegion;
import com.macro.mall.service.SysRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SysRegionServiceImpl implements SysRegionService {
    @Autowired
    private SysRegionMapper regionMapper;

    @Override
    public int create(SysRegion region) {
        // 检查区域名称是否为空
        if (region.getRegionName() == null || region.getRegionName().trim().isEmpty()) {
            throw new IllegalArgumentException(ResultCode.REGION_NAME_EMPTY.getMessage());
        }
        
        // 检查区域名称是否已存在
        SysRegion existingRegion = regionMapper.selectByRegionName(region.getRegionName().trim());
        if (existingRegion != null) {
            throw new IllegalArgumentException(ResultCode.REGION_NAME_DUPLICATE.getMessage());
        }
        
        // 检查区域级别是否有效
        if (region.getRegionLevel() == null || region.getRegionLevel() < 1 || region.getRegionLevel() > 5) {
            throw new IllegalArgumentException(ResultCode.REGION_LEVEL_INVALID.getMessage());
        }
        
        // 检查非省级区域是否提供了父级编码
        if (region.getRegionLevel() > 1 && (region.getParentCode() == null || region.getParentCode().isEmpty())) {
            throw new IllegalArgumentException(ResultCode.REGION_PARENT_NOT_FOUND.getMessage());
        }
        
        // 如果是非省级区域，验证父级编码是否存在
        if (region.getRegionLevel() > 1) {
            SysRegion parentRegion = regionMapper.selectByRegionCode(region.getParentCode());
            if (parentRegion == null) {
                throw new IllegalArgumentException(ResultCode.REGION_PARENT_NOT_FOUND.getMessage());
            }
            
            // 允许跨级，但要确保父级的级别小于当前级别
            if (parentRegion.getRegionLevel() >= region.getRegionLevel()) {
                throw new IllegalArgumentException(ResultCode.REGION_PARENT_LEVEL_ERROR.getMessage());
            }
            
            // 检查父级编码格式是否正确（除了父级有效位之外都应该是0）
            String parentCode = region.getParentCode();
            int parentLevel = parentRegion.getRegionLevel();
            String validPart = parentCode.substring(0, parentLevel * 3);
            String zeroPart = parentCode.substring(parentLevel * 3);
            if (!zeroPart.matches("0+")) {
                throw new IllegalArgumentException(ResultCode.REGION_CODE_INVALID.getMessage());
            }
        }
        
        // 生成区域编码
        String regionCode = generateRegionCode(region.getParentCode(), region.getRegionLevel());
        
        // 设置其他字段
        region.setRegionCode(regionCode);
        region.setCreateTime(new Date());
        region.setUpdateTime(new Date());
        if (region.getStatus() == null) {
            region.setStatus(1); // 默认启用
        }
        if (region.getSort() == null) {
            region.setSort(0); // 默认排序值
        }
        
        return regionMapper.insert(region);
    }

    @Override
    public List<SysRegion> getRegionByCode(String regionCode) {
        if (regionCode == null || regionCode.length() != 15) {
            return new ArrayList<>();
        }

        List<SysRegion> result = new ArrayList<>();
        Set<String> addedCodes = new HashSet<>();  // 用于跟踪已添加的编码
        
        // 计算实际查询的级别深度
        List<Integer> validLevels = new ArrayList<>();  // 存储所有非000的级别
        for (int i = 1; i <= 5; i++) {
            String levelCode = regionCode.substring((i-1)*3, i*3);
            if (!levelCode.equals("000")) {
                validLevels.add(i);
            }
        }
        
        // 如果没有有效级别，返回空列表
        if (validLevels.isEmpty()) {
            return result;
        }
        
        // 从第一级开始查询到最后一个有效级别
        for (int i = 1; i <= validLevels.get(validLevels.size()-1); i++) {
            // 截取到当前级别的编码，后面补0
            String currentCode = regionCode.substring(0, i * 3) + "0".repeat(15 - i * 3);
            
            // 如果编码已经添加过，跳过
            if (addedCodes.contains(currentCode)) {
                continue;
            }
            
            // 查询当前级别的区域
            SysRegion region = regionMapper.selectByRegionCode(currentCode);
            if (region != null) {
                result.add(region);
                addedCodes.add(currentCode);  // 记录已添加的编码
            } else {
                // 如果某一级找不到，就不再继续查找更低级别
                break;
            }
        }
        
        return result;
    }

    @Override
    public String generateRegionCode(String parentCode, Integer regionLevel) {
        // 每个级别占3位，总长度15位
        if (regionLevel == 1) {
            // 获取当前最大的省级编码
            String maxCode = regionMapper.selectMaxCodeByLevel(1);
            if (maxCode == null) {
                String code = "001000000000000";
                // 检查编码是否已存在
                while (regionMapper.selectByRegionCode(code) != null) {
                    int nextCode = Integer.parseInt(code.substring(0, 3)) + 1;
                    code = String.format("%03d000000000000", nextCode);
                }
                return code;
            }
            // 提取前3位，转为整数后加1
            int nextCode = Integer.parseInt(maxCode.substring(0, 3)) + 1;
            String code = String.format("%03d000000000000", nextCode);
            // 检查编码是否已存在，如果存在则继续递增
            while (regionMapper.selectByRegionCode(code) != null) {
                nextCode++;
                code = String.format("%03d000000000000", nextCode);
            }
            return code;
        } else {
            if (parentCode == null) {
                throw new IllegalArgumentException(ResultCode.REGION_PARENT_NOT_FOUND.getMessage());
            }
            // 根据父级编码和当前级别获取最大编码
            String maxCode = regionMapper.selectMaxCodeByParentAndLevel(parentCode, regionLevel);
            if (maxCode == null) {
                // 如果没有现有编码，返回父级编码+001+后续0
                String code = parentCode.substring(0, (regionLevel-1)*3) + "001" + "0".repeat(15-regionLevel*3);
                // 检查编码是否已存在
                while (regionMapper.selectByRegionCode(code) != null) {
                    int start = (regionLevel-1)*3;
                    int nextCode = Integer.parseInt(code.substring(start, start+3)) + 1;
                    code = parentCode.substring(0, start) + 
                           String.format("%03d", nextCode) + 
                           "0".repeat(15-regionLevel*3);
                }
                return code;
            }
            // 获取当前级别的3位编码
            int start = (regionLevel-1)*3;
            int nextCode = Integer.parseInt(maxCode.substring(start, start+3)) + 1;
            // 组合新编码：父级编码前缀 + 当前级别新编码 + 后续0
            String code = parentCode.substring(0, start) + 
                         String.format("%03d", nextCode) + 
                         "0".repeat(15-regionLevel*3);
            // 检查编码是否已存在，如果存在则继续递增
            while (regionMapper.selectByRegionCode(code) != null) {
                nextCode++;
                code = parentCode.substring(0, start) + 
                       String.format("%03d", nextCode) + 
                       "0".repeat(15-regionLevel*3);
            }
            return code;
        }
    }

    @Override
    public String getRegionCodeByAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        // 分割地址
        String[] parts = address.split("(?=[省市区县乡镇街道])|(?<=[省市区县乡镇街道])");
        String result = null;
        int maxLevel = 0;
        
        // 处理分割后的地址部分
        StringBuilder fullName = new StringBuilder();  // 用于累积完整地址名
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty() || part.matches("^(省|市|区|县|乡|镇|街道)$")) {
                if (!fullName.isEmpty() && part.matches("^(省|市|区|县|乡|镇|街道)$")) {
                    fullName.append(part);
                    // 尝试查询累积的地址
                    SysRegion region = regionMapper.selectByRegionName(fullName.toString());
                    if (region != null && region.getRegionLevel() > maxLevel) {
                        result = region.getRegionCode();
                        maxLevel = region.getRegionLevel();
                    }
                    fullName.setLength(0);  // 清空，准备下一个地址部分
                }
                continue;
            }
            
            fullName.append(part);
        }
        
        // 处理最后一个地址部分（如果有）
        if (fullName.length() > 0) {
            SysRegion region = regionMapper.selectByRegionName(fullName.toString());
            if (region != null && region.getRegionLevel() > maxLevel) {
                result = region.getRegionCode();
                maxLevel = region.getRegionLevel();
            }
        }
        
        return result;
    }

    @Override
    public List<SysRegion> listByLevel(Integer level) {
        if (level == null || level < 1 || level > 5) {
            return new ArrayList<>();
        }
        return regionMapper.selectByRegionLevel(level);
    }

    @Override
    public List<SysRegion> listNextLevel(String regionCode) {
        if (regionCode == null || regionCode.length() != 15) {
            return new ArrayList<>();
        }
        
        // 获取当前区域信息
        SysRegion currentRegion = regionMapper.selectByRegionCode(regionCode);
        if (currentRegion == null) {
            return new ArrayList<>();
        }
        
        // 计算下一级别
        int nextLevel = currentRegion.getRegionLevel() + 1;
        
        // 如果当前是区县级(3级)，则下一级可以是街道(5级)，跳过乡镇(4级)
        if (currentRegion.getRegionLevel() == 3) {
            List<SysRegion> regions = regionMapper.selectByParentCode(regionCode);
            // 按sort和id排序
            regions.sort((a, b) -> {
                if (a.getSort() == null || b.getSort() == null) {
                    return a.getId().compareTo(b.getId());
                }
                int sortCompare = a.getSort().compareTo(b.getSort());
                return sortCompare != 0 ? sortCompare : a.getId().compareTo(b.getId());
            });
            return regions;
        }
        
        // 如果已经是最后一级(5级)或者超出范围，返回空列表
        if (nextLevel > 5) {
            return new ArrayList<>();
        }
        
        // 查询下一级区域列表
        return regionMapper.selectByParentCodeAndLevel(regionCode, nextLevel);
    }

    @Override
    public int update(Long id, SysRegion region) {
        // 检查区域是否存在
        SysRegion existingRegion = regionMapper.selectById(id);
        if (existingRegion == null) {
            return 0;
        }
        
        // 只允许修改部分字段
        SysRegion updateRegion = new SysRegion();
        updateRegion.setId(id);
        updateRegion.setRegionName(region.getRegionName());
        updateRegion.setStatus(region.getStatus());
        updateRegion.setSort(region.getSort());
        updateRegion.setUpdateTime(new Date());
        
        // 如果要修改区域名称，检查是否重复
        if (region.getRegionName() != null) {
            SysRegion existingName = regionMapper.selectByRegionName(region.getRegionName().trim());
            if (existingName != null && !existingName.getId().equals(id)) {
                throw new IllegalArgumentException(ResultCode.REGION_NAME_DUPLICATE.getMessage());
            }
        }
        
        return regionMapper.updateById(updateRegion);
    }

    @Override
    public int delete(Long id) {
        // 检查区域是否存在
        SysRegion region = regionMapper.selectById(id);
        if (region == null) {
            return 0;
        }
        
        // 检查是否有下级区域
        List<SysRegion> children = regionMapper.selectByParentCode(region.getRegionCode());
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("存在下级区域，无法删除");
        }
        
        return regionMapper.deleteById(id);
    }
} 