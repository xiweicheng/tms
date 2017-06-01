package com.lhjz.portal.component.core.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.lhjz.portal.component.core.MailQueue;
import com.lhjz.portal.component.core.model.MailItem;

import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class MailQueueImpl implements MailQueue {

	BlockingQueue<MailItem> mailQueue = new LinkedBlockingQueue<MailItem>();

	@Override
	public void put(MailItem mailItem) throws InterruptedException {
		log.info("邮件入队列!");
		this.mailQueue.put(mailItem);
	}

	@Override
	public MailItem take() throws InterruptedException {
		log.info("邮件出队列!");
		return this.mailQueue.take();
	}

	@Override
	public MailItem poll() {
		return this.mailQueue.poll();
	}

	@Override
	public boolean offer(MailItem mailItem) {
		return this.mailQueue.offer(mailItem);
	}

}
