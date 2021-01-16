/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatReply;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
@Data
@Builder
public class UuidBody {
	
	private String type;

	private ChatChannel chatChannel;
	
	private ChatDirect chatDirect;
	
	private ChatReply chatReply;
	
}
