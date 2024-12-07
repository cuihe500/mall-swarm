package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.SysRegionMapper;
import com.macro.mall.model.SysRegion;
import com.macro.mall.portal.service.SysRegionPortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 前台区域管理Service实现类
 */
@Service
public class SysRegionPortalServiceImpl implements SysRegionPortalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysRegionPortalServiceImpl.class);

    @Autowired
    private SysRegionMapper regionMapper;

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
    public List<SysRegion> listByLevel(Integer level) {
        if (level == null || level < 1 || level > 5) {
            return new ArrayList<>();
        }
        return regionMapper.selectByRegionLevel(level);
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
    public List<SysRegion> listNextLevel(String regionCode) {
        if (regionCode == null || regionCode.length() != 15) {
            return new ArrayList<>();
        }
        
        // 确定当前编码的级别
        int currentLevel = 0;
        for (int i = 1; i <= 5; i++) {
            String levelCode = regionCode.substring((i-1)*3, i*3);
            if (!levelCode.equals("000")) {
                currentLevel = i;
            } else {
                break; // 一旦遇到000就停止，因为高级别编码后面必须都是000
            }
        }
        
        LOGGER.info("当前区域编码: {}, 级别: {}", regionCode, currentLevel);
        
        if (currentLevel == 0) {
            return new ArrayList<>();
        }
        
        // 计算下一级别
        int nextLevel = currentLevel + 1;
        
        // 如果当前是区县级(3级)，则下一级可以是街道(5级)，跳过乡镇(4级)
        if (currentLevel == 3) {
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
        List<SysRegion> regions = regionMapper.selectByParentCodeAndLevel(regionCode, nextLevel);
        LOGGER.info("查询到下级区域数量: {}", regions.size());
        return regions;
    }
} 