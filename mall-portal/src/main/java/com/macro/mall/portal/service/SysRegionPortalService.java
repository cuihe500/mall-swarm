package com.macro.mall.portal.service;

import com.macro.mall.model.SysRegion;
import java.util.List;

/**
 * 前台区域管理Service
 */
public interface SysRegionPortalService {
    /**
     * 根据区域编码查询区域信息
     */
    List<SysRegion> getRegionByCode(String regionCode);

    /**
     * 根据区域级别获取所有区域信息
     */
    List<SysRegion> listByLevel(Integer level);

    /**
     * 根据地址名称获取地址编号
     */
    String getRegionCodeByAddress(String address);

    /**
     * 根据区域编码获取下一级区域列表
     * @param regionCode 区域编码
     * @return 下一级区域列表
     */
    List<SysRegion> listNextLevel(String regionCode);
} 