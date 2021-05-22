package com.lhjz.portal.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程处理工具类.
 *
 * @author xiweicheng
 * @version 1.0
 * @creation 2014年4月17日 下午10:53:22
 * @modification 2014年4月17日 下午10:53:22
 * @company
 */
public final class ThreadUtil {

    private static ThreadLocal<String> tl = new ThreadLocal<>();

    private ThreadUtil() {
    }

    private static ExecutorService pool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024), new ThreadFactoryBuilder()
            .setNameFormat("single-pool-%d").build(), new ThreadPoolExecutor.AbortPolicy());

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
     * @param command
     * @author xiweicheng
     * @creation 2014年4月17日 下午10:52:57
     * @modification 2014年4月17日 下午10:52:57
     */
    public static void exec(Runnable... commands) {

        if (commands.length > 0) {
            for (Runnable runnable : commands) {
                pool.execute(runnable);
            }
        }
    }

}
