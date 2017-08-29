package com.lhjz.portal.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

/**
 * Query基类<br>
 * 
 * @describe：封装JPA CriteriaBuilder查询条件
 */
@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
public class Query implements Serializable {

	private static final long serialVersionUID = 5064932771068929342L;

	private static Logger log = Logger.getLogger(Query.class);

	private EntityManager entityManager;

	/** 要查询的模型对象 */
	private Class clazz;

	/** 查询条件列表 */
	private Root from;

	private List<Predicate> predicates;

	private CriteriaQuery criteriaQuery;

	private CriteriaBuilder criteriaBuilder;

	/** 排序方式列表 */
	private List<Order> orders;

	/** 关联模式 */
	private Map<String, Query> subQuery;

	private Map<String, Query> linkQuery;

	private String projection;

	/** 或条件 */
	private List<Query> orQuery;

	private String groupBy;

	private Query() {
	}

	private Query(Class clazz, EntityManager entityManager) {
		this.clazz = clazz;
		this.entityManager = entityManager;
		this.criteriaBuilder = this.entityManager.getCriteriaBuilder();
		this.criteriaQuery = criteriaBuilder.createQuery(this.clazz);
		this.from = criteriaQuery.from(this.clazz);
		this.predicates = new ArrayList();
		this.orders = new ArrayList();
	}

	/** 通过类创建查询条件 */
	public static Query forClass(Class clazz, EntityManager entityManager) {
		return new Query(clazz, entityManager);
	}

	/** 增加子查询 */
	private void addSubQuery(String propertyName, Query query) {
		if (this.subQuery == null)
			this.subQuery = new HashMap();

		if (query.projection == null)
			throw new RuntimeException("子查询字段未设置");

		this.subQuery.put(propertyName, query);
	}

	private void addSubQuery(Query query) {
		addSubQuery(query.projection, query);
	}

	/** 增关联查询 */
	public void addLinkQuery(String propertyName, Query query) {
		if (this.linkQuery == null)
			this.linkQuery = new HashMap();

		this.linkQuery.put(propertyName, query);
	}

	/** 相等 */
	public void eq(String propertyName, Object value) {
		if (isNullOrEmpty(value))
			return;
		this.predicates.add(criteriaBuilder.equal(from.get(propertyName), value));
	}

	private boolean isNullOrEmpty(Object value) {
		if (value instanceof String) {
			return value == null || "".equals(value);
		}
		return value == null;
	}

	public void or(List<String> propertyName, Object value) {
		if (isNullOrEmpty(value))
			return;
		if ((propertyName == null) || (propertyName.size() == 0))
			return;
		Predicate predicate = criteriaBuilder.or(criteriaBuilder.equal(from.get(propertyName.get(0)), value));
		for (int i = 1; i < propertyName.size(); ++i)
			predicate = criteriaBuilder.or(predicate, criteriaBuilder.equal(from.get(propertyName.get(i)), value));
		this.predicates.add(predicate);
	}

	public void orLike(List<String> propertyName, String value) {
		if (isNullOrEmpty(value) || (propertyName.size() == 0))
			return;
		if (value.indexOf("%") < 0)
			value = "%" + value + "%";
		Predicate predicate = criteriaBuilder.or(criteriaBuilder.like(from.get(propertyName.get(0)), value.toString()));
		for (int i = 1; i < propertyName.size(); ++i)
			predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(from.get(propertyName.get(i)), value));
		this.predicates.add(predicate);
	}

	/** 空 */
	public void isNull(String propertyName) {
		this.predicates.add(criteriaBuilder.isNull(from.get(propertyName)));
	}

	/** 非空 */
	public void isNotNull(String propertyName) {
		this.predicates.add(criteriaBuilder.isNotNull(from.get(propertyName)));
	}

	/** 不相等 */
	public void notEq(String propertyName, Object value) {
		if (isNullOrEmpty(value)) {
			return;
		}
		this.predicates.add(criteriaBuilder.notEqual(from.get(propertyName), value));
	}

	/**
	 * not in
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            值集合
	 */
	public void notIn(String propertyName, Collection value) {
		if ((value == null) || (value.size() == 0)) {
			return;
		}
		Iterator iterator = value.iterator();
		In in = criteriaBuilder.in(from.get(propertyName));
		while (iterator.hasNext()) {
			in.value(iterator.next());
		}
		this.predicates.add(criteriaBuilder.not(in));
	}

	/**
	 * 模糊匹配
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 */
	public void like(String propertyName, String value) {
		if (isNullOrEmpty(value))
			return;
		if (value.indexOf("%") < 0)
			value = "%" + value + "%";
		this.predicates.add(criteriaBuilder.like(from.get(propertyName), value));
	}

	/**
	 * 时间区间查询
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param lo
	 *            属性起始值
	 * @param go
	 *            属性结束值
	 */
	public void between(String propertyName, Date lo, Date go) {
		if (!isNullOrEmpty(lo) && !isNullOrEmpty(go)) {
			this.predicates.add(criteriaBuilder.between(from.get(propertyName), lo, go));
		}

		// if (!isNullOrEmpty(lo) && !isNullOrEmpty(go)) {
		// this.predicates.add(criteriaBuilder.lessThan(from.get(propertyName),
		// new DateTime(lo).toString()));
		// }
		// if (!isNullOrEmpty(go)) {
		// this.predicates.add(criteriaBuilder.greaterThan(from.get(propertyName),
		// new DateTime(go).toString()));
		// }

	}

	public void between(String propertyName, Number lo, Number go) {
		if (!(isNullOrEmpty(lo)))
			ge(propertyName, lo);

		if (!(isNullOrEmpty(go)))
			le(propertyName, go);
	}

	/**
	 * 小于等于
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 */
	public void le(String propertyName, Number value) {
		if (isNullOrEmpty(value)) {
			return;
		}
		this.predicates.add(criteriaBuilder.le(from.get(propertyName), value));
	}

	/**
	 * 小于
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 */
	public void lt(String propertyName, Number value) {
		if (isNullOrEmpty(value)) {
			return;
		}
		this.predicates.add(criteriaBuilder.lt(from.get(propertyName), value));
	}

	/**
	 * 大于等于
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 */
	public void ge(String propertyName, Number value) {
		if (isNullOrEmpty(value)) {
			return;
		}
		this.predicates.add(criteriaBuilder.ge(from.get(propertyName), value));
	}

	/**
	 * 大于
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 */
	public void gt(String propertyName, Number value) {
		if (isNullOrEmpty(value)) {
			return;
		}
		this.predicates.add(criteriaBuilder.gt(from.get(propertyName), value));
	}

	/**
	 * in
	 * 
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            值集合
	 */
	public void in(String propertyName, Collection value) {
		if ((value == null) || (value.size() == 0)) {
			return;
		}
		Iterator iterator = value.iterator();
		In in = criteriaBuilder.in(from.get(propertyName));
		while (iterator.hasNext()) {
			in.value(iterator.next());
		}
		this.predicates.add(in);
	}

	/** 直接添加JPA内部的查询条件,用于应付一些复杂查询的情况,例如或 */
	public void addCriterions(Predicate predicate) {
		this.predicates.add(predicate);
	}

	/**
	 * 创建查询条件
	 * 
	 * @return JPA离线查询
	 */
	public CriteriaQuery newCriteriaQuery() {
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		if (!isNullOrEmpty(groupBy)) {
			criteriaQuery.groupBy(from.get(groupBy));
		}
		if (this.orders != null) {
			criteriaQuery.orderBy(orders);
		}
		addLinkCondition(this);
		return criteriaQuery;
	}

	private void addLinkCondition(Query query) {

		Map subQuery = query.linkQuery;
		if (subQuery == null)
			return;

		for (Iterator queryIterator = subQuery.keySet().iterator(); queryIterator.hasNext();) {
			String key = (String) queryIterator.next();
			Query sub = (Query) subQuery.get(key);
			from.join(key);
			criteriaQuery.where(sub.predicates.toArray(new Predicate[0]));
			addLinkCondition(sub);
		}
	}

	public void addOrder(String propertyName, String order) {
		if (order == null || propertyName == null)
			return;

		if (this.orders == null)
			this.orders = new ArrayList();

		if (order.equalsIgnoreCase("asc"))
			this.orders.add(criteriaBuilder.asc(from.get(propertyName)));
		else if (order.equalsIgnoreCase("desc"))
			this.orders.add(criteriaBuilder.desc(from.get(propertyName)));
	}

	public void setOrder(String propertyName, String order) {
		this.orders = null;
		addOrder(propertyName, order);
	}

	public Class getModleClass() {
		return this.clazz;
	}

	public String getProjection() {
		return this.projection;
	}

	public void setProjection(String projection) {
		this.projection = projection;
	}

	public Class getClazz() {
		return this.clazz;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

	public Root getFrom() {
		return from;
	}

	public List<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<Predicate> predicates) {
		this.predicates = predicates;
	}

	public CriteriaQuery getCriteriaQuery() {
		return criteriaQuery;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}

	public void setFetchModes(List<String> fetchField, List<String> fetchMode) {

	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

}