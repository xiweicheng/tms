package com.lhjz.portal.component.core;

import com.lhjz.portal.component.core.model.MailItem;

public interface MailQueue {

	void put(MailItem mailItem) throws InterruptedException;
	
	boolean offer(MailItem mailItem);
	
	MailItem take() throws InterruptedException;
	
	MailItem poll();
	
}
