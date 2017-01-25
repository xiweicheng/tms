/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Translate;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface LabelRepository extends JpaRepository<Label, Long> {

	Label findOneByNameAndTranslate(String name, Translate translate);

	List<Label> findByCreator(String creator);

	List<Label> findByChat(Chat chat);

	Label findOneByNameAndChat(String name, Chat chat);
	
	@Query(value = "SELECT * FROM label WHERE creator = ?1 GROUP BY name;", nativeQuery = true)
	List<Label> findByCreatorGroupByName(String creator);
	
	@Query(value = "SELECT * FROM `label` WHERE chat_id  IS NOT NULL GROUP BY `name`", nativeQuery = true)
	List<Label> queryWikiLabels();

}
