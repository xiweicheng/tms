/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.File;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.ToType;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface FileRepository extends JpaRepository<File, Long> {

	File findTopByUuidName(String uuidName);

	Page<File> findByToTypeAndToIdAndTypeAndNameContaining(ToType toType, String toId, FileType fileType, String search,
			Pageable pageable);

	Page<File> findByToTypeAndUsernameAndToIdAndTypeAndNameContaining(ToType toType, String username, String toId,
			FileType fileType, String search, Pageable pageable);
}
