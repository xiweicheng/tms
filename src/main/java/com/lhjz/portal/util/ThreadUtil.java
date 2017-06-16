package com.lhjz.portal.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程处理工具类.
 * 
 * @creation 2014年4月17日 下午10:53:22
 * @modification 2014年4月17日 下午10:53:22
 * @company
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class ThreadUtil {

	private static ThreadLocal<String> tl = new ThreadLocal<>();

	private static ExecutorService pool = Executors.newCachedThreadPool();

	public static void setCurrentAuditor(String username) {
		tl.set(username);
	}

	public static String getCurrentAuditor() {
		return tl.get();
	}

	public static void clearCurrentAuditor() {
		tl.remove();
	}

	/**
	 * 执行线程池任务.
	 * 
	 * @author xiweicheng
	 * @creation 2014年4月17日 下午10:52:57
	 * @modification 2014年4月17日 下午10:52:57
	 * @param command
	 */
	public static void exec(Runnable... commands) {

		if (commands.length > 0) {
			for (Runnable runnable : commands) {
				pool.execute(runnable);
			}
		}
	}

}
