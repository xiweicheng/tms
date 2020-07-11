/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service;

/**
 * @author xi
 *
 */
public interface BlogLockService {
	
	Boolean isRealLock(Long blogId);
	
	Boolean isLock(Long blogId);
	
	Boolean isLockBy(String username, Long blogId);

	Boolean lockBy(String username, Long blogId);

	Boolean unlockBy(String username, Long blogId);
	
	Boolean lockForce(String username, Long blogId);
	
	Boolean unlockForce(Long blogId);

}
