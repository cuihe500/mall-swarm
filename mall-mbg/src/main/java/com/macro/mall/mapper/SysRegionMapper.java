package com.macro.mall.mapper;

import com.macro.mall.model.SysRegion;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 区域管理Mapper
 */
public interface SysRegionMapper {
    /**
     * 插入区域信息
     */
    int insert(SysRegion record);

    /**
     * 根据区域编码查询区域信息
     */
    SysRegion selectByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 根据区域级别查询最大的区域编码
     */
    String selectMaxCodeByLevel(@Param("level") Integer level);

    /**
     * 根据父级编码和区域级别查询最大的区域编码
     */
    String selectMaxCodeByParentAndLevel(@Param("prefix") String prefix, @Param("level") Integer level);

    /**
     * 根据区域名称查询区域信息
     */
    SysRegion selectByRegionName(@Param("regionName") String regionName);

    /**
     * 根据区域级别查询所有区域信息
     */
    List<SysRegion> selectByRegionLevel(@Param("regionLevel") Integer regionLevel);

    /**
     * 根据父级编码查询区域列表
     */
    List<SysRegion> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据父级编码和级别查询区域列表
     */
    List<SysRegion> selectByParentCodeAndLevel(@Param("parentCode") String parentCode, @Param("level") Integer level);

    /**
     * 根据ID查询区域
     */
    SysRegion selectById(@Param("id") Long id);

    /**
     * 根据ID更新区域
     */
    int updateById(SysRegion region);

    /**
     * 根据ID删除区域
     */
    int deleteById(@Param("id") Long id);
} 