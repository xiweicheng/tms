/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.File;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.Status;
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

	List<File> findByAtId(String atId);

	File findTopByUuidAndStatusNot(String uuid, Status status);

	Page<File> findByToTypeAndToIdAndTypeAndStatusNotAndNameContainingIgnoreCase(ToType toType, String toId,
			FileType fileType, Status status, String search, Pageable pageable);

	Page<File> findByToTypeAndUsernameAndToIdAndTypeAndStatusNotAndNameContainingIgnoreCase(ToType toType,
			String username, String toId, FileType fileType, Status status, String search, Pageable pageable);

	@Query(value = "SELECT * FROM file WHERE to_type IS NULL", nativeQuery = true)
	List<File> queryByToTypeIsNull();

	@Query(value = "SELECT * FROM file WHERE to_type IS NULL AND type = 'Attachment'", nativeQuery = true)
	List<File> queryAttachmentByToTypeIsNull();

	@Transactional
	@Modifying
	@Query(value = "UPDATE file SET to_type = 'Channel', to_id = ?1 WHERE id = ?2", nativeQuery = true)
	int updateFileToChannel(String toId, Long id);

	@Transactional
	@Modifying
	@Query(value = "UPDATE file SET to_type = 'User', to_id = ?1 WHERE id = ?2", nativeQuery = true)
	int updateFileToUser(String toId, Long id);

	@Transactional
	@Modifying
	@Query(value = "UPDATE file SET status = 'Deleted' WHERE at_id = ?1", nativeQuery = true)
	int updateFileByAtId(String atId);

	List<File> findTop40ByTypeAndStatusNot(FileType type, Status status, Sort sort);

}
