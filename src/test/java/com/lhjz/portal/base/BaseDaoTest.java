/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.base;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;

import com.lhjz.portal.Application;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午7:54:29
 * 
 */
@SpringApplicationConfiguration(classes = Application.class)
public abstract class BaseDaoTest extends
		AbstractTransactionalTestNGSpringContextTests {

}
