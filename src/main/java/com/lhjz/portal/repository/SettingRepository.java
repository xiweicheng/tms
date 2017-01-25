/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Setting;
import com.lhjz.portal.pojo.Enum.SettingType;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface SettingRepository extends JpaRepository<Setting, Long> {

	Setting findOneBySettingType(SettingType settingType);

}
