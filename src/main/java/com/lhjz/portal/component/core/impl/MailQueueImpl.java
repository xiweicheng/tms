package com.lhjz.portal.component.core.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.lhjz.portal.component.core.MailQueue;
import com.lhjz.portal.component.core.model.MailItem;

import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class MailQueueImpl implements MailQueue {

	ConcurrentLinkedQueue<MailItem> mailQueue = new ConcurrentLinkedQueue<MailItem>();

	@Override
	public MailItem poll() {
		log.info("邮件出队列!");
		return this.mailQueue.poll();
	}

	@Override
	public boolean offer(MailItem mailItem) {
		log.info("邮件入队列!");
		return this.mailQueue.offer(mailItem);
	}

}
