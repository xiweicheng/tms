package com.lhjz.portal.model;

import java.io.Serializable;
import java.util.List;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogSearchResult implements Serializable {

	private static final long serialVersionUID = 4908485605547721160L;

	List<Blog> blogs;
	
	List<Comment> comments;

}
