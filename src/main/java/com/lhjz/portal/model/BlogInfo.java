/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import com.lhjz.portal.entity.Blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogInfo {

	Blog blog;
	Blog pre;
	Blog next;

}
