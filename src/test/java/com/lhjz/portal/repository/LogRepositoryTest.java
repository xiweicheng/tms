package com.lhjz.portal.repository;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.lhjz.portal.entity.Log;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LogRepositoryTest {

	@Autowired
	LogRepository logRepository;

	@Test
	public void search() {

		List<Target> targets = Arrays.asList(Target.Blog, Target.Comment);
		List<Log> logs = logRepository.findTop50ByStatusNotAndTargetInAndIdLessThanOrderByIdDesc(Status.Deleted,
				targets, 200L);
		System.out.println(logs.size());
	}

	@Test
	public void search2() {

		List<Target> targets = Arrays.asList(Target.Blog, Target.Comment);
		List<Log> logs = logRepository.findTop50ByStatusNotAndTargetInOrderByIdDesc(Status.Deleted, targets);
		System.out.println(logs.size());
	}

}
