package com.lhjz.portal.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * hibernate util.
 *
 * @author xiweicheng
 * @version 1.0
 * @creation 2013-10-13 上午8:04:24
 * @modification
 * @company Canzs
 */
public class HibernateUtil {

    public static final SessionFactory SESSION_FACTORY;

    private HibernateUtil() {
    }

    static {
        try {
            // 采用默认的hibernate.cfg.xml来启动一个Configuration的实例
            Configuration configuration = new Configuration().configure();
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            // 由Configuration的实例来创建一个SessionFactory实例
            SESSION_FACTORY = configuration.buildSessionFactory(builder.build());
        } catch (Exception ex) {
            // Make sure you log the exception, as it might be swallowed
            throw new ExceptionInInitializerError(ex);
        }
    }

    // ThreadLocal并不是线程本地化的实现,而是线程局部变量。也就是说每个使用该变量的线程都必须为
    // 该变量提供一个副本,每个线程改变该变量的值仅仅是改变该副本的值,而不会影响其他线程的该变量
    // 的值.

    // ThreadLocal是隔离多个线程的数据共享，不存在多个线程之间共享资源,因此不再需要对线程同步
    public static final ThreadLocal<Session> SESSION_THREAD_LOCAL = new ThreadLocal<Session>();

    public static Session currentSession() throws HibernateException {
        Session s = SESSION_THREAD_LOCAL.get();
        // 如果该线程还没有Session,则创建一个新的Session
        if (s == null) {
            s = SESSION_FACTORY.openSession();
            // 将获得的Session变量存储在ThreadLocal变量session里
            SESSION_THREAD_LOCAL.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException {
        Session s = SESSION_THREAD_LOCAL.get();
        if (s != null) {
            s.close();
        }
        SESSION_THREAD_LOCAL.remove();
    }
}