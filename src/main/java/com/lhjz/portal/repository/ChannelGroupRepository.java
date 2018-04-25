/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.ChannelGroup;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.entity.Channel;
import java.util.List;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChannelGroupRepository extends JpaRepository<ChannelGroup, Long> {

	ChannelGroup findOneByNameAndStatusNot(String name, Status status);

	List<ChannelGroup> findByChannelAndStatusNot(Channel channel, Status status);

}
