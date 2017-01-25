/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
public class Search {

	private List<Object> list = new ArrayList<Object>();

	private Search() {
	}

	public static Search instance() {
		return new Search();
	}

	public Search add(Object... objs) {

		for (Object object : objs) {
			list.add(object);
		}

		return this;
	}

	public Search translate(Translate translate) {

		this.add(translate.getKey(), translate.getDescription());

		Set<TranslateItem> translateItems = translate.getTranslateItems();
		for (TranslateItem translateItem : translateItems) {
			this.add(translateItem.getContent());
		}

		Set<Label> labels = translate.getLabels();
		for (Label label : labels) {
			this.add(label.getName());
		}

		return this;
	}

	@Override
	public String toString() {
		return StringUtil.join2(StringUtil.SEARCH_SEPARATOR, list);
	}

}
