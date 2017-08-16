package com.lhjz.portal.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.lhjz.portal.entity.Task;
import com.lhjz.portal.pojo.Enum.Status;

public class TaskSpecs {

	public static Specification<Task> search() {
		return new Specification<Task>() {

			@Override
			public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				Predicate p = cb.notEqual(root.get("status"), Status.Deleted);

				return p;
			}

		};
	}

}
