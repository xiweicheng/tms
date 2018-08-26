/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Dir;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface DirRepository extends JpaRepository<Dir, Long> {

	Dir findTop1BySpaceAndNameAndStatus(Space space, String name, Status status);

}