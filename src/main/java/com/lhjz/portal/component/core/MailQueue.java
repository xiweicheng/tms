package com.lhjz.portal.component.core;

import com.lhjz.portal.component.core.model.MailItem;

public interface MailQueue {

	boolean offer(MailItem mailItem);
	
	MailItem poll();
	
}
