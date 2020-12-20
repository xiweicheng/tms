/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface TranslateRepository extends JpaRepository<Translate, Long> {

	List<Translate> findByKeyAndProject(String key, Project project);

	List<Translate> findByProject(Project project);

	Page<Translate> findByProject(Project project, Pageable pageable);

	Page<Translate> findById(Long id, Pageable pageable);

	Page<Translate> findByProjectAndCreator(Project project, String creator,
			Pageable pageable);

	Page<Translate> findByCreator(String creator, Pageable pageable);

	Page<Translate> findByProjectAndSearchLike(Project project, String search,
			Pageable pageable);

	Page<Translate> findBySearchLike(String search, Pageable pageable);

	Page<Translate> findByProjectAndStatus(Project project, Status status,
			Pageable pageable);

	Page<Translate> findByStatus(Status status, Pageable pageable);

	@Query(value = "SELECT * FROM translate WHERE id IN (SELECT DISTINCT t.id FROM translate_item AS ti INNER JOIN translate AS t ON ti.translate_id = t.id WHERE ti.language_id = ?1 AND t.project_id = ?2 AND ti.content = '') ORDER BY create_date DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
	List<Translate> queryUnTranslatedByProject(Long languageId, Long projectId,
			int from, int size);

	@Query(value = "SELECT COUNT(*) FROM translate WHERE id IN (SELECT DISTINCT t.id FROM translate_item AS ti INNER JOIN translate AS t ON ti.translate_id = t.id WHERE ti.language_id = ?1 AND t.project_id = ?2 AND ti.content = '')", nativeQuery = true)
	long countUnTranslatedByProject(Long languageId, Long projectId);

	@Query(value = "SELECT * FROM translate WHERE id IN (SELECT DISTINCT t.id FROM translate_item AS ti INNER JOIN translate AS t ON ti.translate_id = t.id WHERE ti.language_id = ?1 AND ti.content = '') ORDER BY create_date DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
	List<Translate> queryUnTranslated(Long languageId, int from, int size);

	@Query(value = "SELECT COUNT(*) FROM translate WHERE id IN (SELECT DISTINCT t.id FROM translate_item AS ti INNER JOIN translate AS t ON ti.translate_id = t.id WHERE ti.language_id = ?1 AND ti.content = '')", nativeQuery = true)
	long countUnTranslated(Long languageId);
}
