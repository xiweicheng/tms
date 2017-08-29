package com.lhjz.portal.specs;

import java.util.Iterator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.jpa.domain.Specification;

import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TStatus;
import com.lhjz.portal.entity.Task;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.TaskSearchParams;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TaskType;

public class TaskSpecs {

	public static Specification<Task> search(TaskSearchParams taskSearchParams) {
		return new Specification<Task>() {

			@Override
			public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				Predicate p = cb.notEqual(root.get("status"), Status.Deleted);

				if (taskSearchParams.getProjects().size() > 0) {
					Iterator<TProject> iterator = taskSearchParams.getProjects().iterator();
					In<Object> in = cb.in(root.get("project"));
					while (iterator.hasNext()) {
						in.value(iterator.next());
					}

					p = cb.and(p, in);
				}

				if (taskSearchParams.getTaskTypes().size() > 0) {
					Iterator<TaskType> iterator = taskSearchParams.getTaskTypes().iterator();
					In<Object> in = cb.in(root.get("type"));
					while (iterator.hasNext()) {
						in.value(iterator.next());
					}

					p = cb.and(p, in);
				}

				if (taskSearchParams.getStates().size() > 0) {
					Iterator<TStatus> iterator = taskSearchParams.getStates().iterator();
					In<Object> in = cb.in(root.get("state"));
					while (iterator.hasNext()) {
						in.value(iterator.next());
					}

					p = cb.and(p, in);
				}

				if (taskSearchParams.getOperator().size() > 0) {
					Iterator<User> iterator = taskSearchParams.getOperator().iterator();
					In<Object> in = cb.in(root.get("operator"));
					while (iterator.hasNext()) {
						in.value(iterator.next());
					}

					p = cb.and(p, in);
				}

				return p;
			}

		};
	}

}
