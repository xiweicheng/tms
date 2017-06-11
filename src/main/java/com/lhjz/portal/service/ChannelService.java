/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.security.User;

/**
 * @author xi
 *
 */
public interface ChannelService {

	void joinAll(String username);

	void joinAll(User user);
	
	Channel createAsSuper(String name, String title);
}
