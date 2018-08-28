package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.locks.Lock;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.util.UUIDConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import chags.scheduler.lock.annotation.SchedulerLock;
import chags.scheduler.lock.jdbc.annotation.EnableJdbcSchedulerLock;
import chags.scheduler.lock.jdbc.annotation.JdbcSchedulerLockConfig;
import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { JdbcSchedulerLockIntegrationTest.TestConfig.class })
@TestPropertySource(properties = { "tablePrefix=TEST_", "region=dsf" })
public class JdbcSchedulerLockIntegrationTest {

	@Autowired
	TestScheduledJob testScheduledJob;

	@Autowired
	LockRegistry lockRegistry;

	@Autowired
	JdbcSchedulerLockConfig jdbcSchedulerLockConfig;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Before
	public void before() {
		testScheduledJob.resetInvocationCount();
	}

	@Test
	public void schedulerShouldRunTheJob() throws InterruptedException {
		Thread.sleep(5000);
		assertThat(testScheduledJob.getInvocationCount()).isGreaterThan(0);
	}

	@Test
	public void schedulerShouldSkipTheJob() throws InterruptedException {
		Lock lock = lockRegistry.obtain(TestScheduledJob.TEST_LOCK);
		try {
			lock.lock();
			Thread.sleep(5000);
			assertThat(testScheduledJob.getInvocationCount()).isEqualTo(0);
		} finally {
			lock.unlock();
		}
	}

	@Test
	public void schedulerShouldConfigureLockFromAnnotationMetaData() {

		assertThat(jdbcSchedulerLockConfig.getTablePrefix()).isEqualTo("TEST_");
		assertThat(jdbcSchedulerLockConfig.getRegion()).isEqualTo("dsf");
		assertThat(jdbcSchedulerLockConfig.getTimeToLive()).isEqualTo(10000);

		String lockKey = "someLock";
		Lock lock = lockRegistry.obtain(lockKey);
		String lockUUID = UUIDConverter.getUUID(lockKey).toString();

		try {
			assertThat(lock.tryLock()).isEqualTo(true);
			assertThat(jdbcTemplate.queryForObject("select count(*) from TEST_LOCK where LOCK_KEY=? and REGION=?",
					Integer.class, lockUUID, jdbcSchedulerLockConfig.getRegion())).isEqualTo(1);
		} finally {
			lock.unlock();
		}

	}

	@Configuration
	@EnableScheduling
	@EnableJdbcSchedulerLock(tablePrefix = "${tablePrefix}", region = "${region}", timeToLive = 10000)
	public static class TestConfig {

		@Bean
		public TestScheduledJob testScheduledJob() {
			return new TestScheduledJob();
		}

		@Bean
		DataSource dataSource() {
			return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addScript("classpath:test_schema.sql")
					.build();
		}

		@Bean
		JdbcTemplate jdbcTemplate(DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}
	}

	@Data
	public static class TestScheduledJob {

		public static final String TEST_LOCK = "testLock";
		int invocationCount;

		@Scheduled(fixedDelay = 1000, initialDelay = 2000)
		@SchedulerLock(name=TEST_LOCK)
		public void runJob() {
			invocationCount++;
		}

		public void resetInvocationCount() {
			this.invocationCount = 0;
		}
	}

}
