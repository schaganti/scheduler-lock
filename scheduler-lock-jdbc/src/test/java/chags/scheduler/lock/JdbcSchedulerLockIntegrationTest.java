package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import chags.scheduler.lock.jdbc.annotation.EnableJdbcSchedulerLocking;
import chags.scheduler.lock.jdbc.annotation.JdbcSchedulerLockConfig;
import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { JdbcSchedulerLockIntegrationTest.TestConfig.class })
@TestPropertySource(properties={"tablePrefix=INT_","region=dsf"})
public class JdbcSchedulerLockIntegrationTest {

	@Autowired
	TestScheduledJob testScheduledJob;

	@Autowired
	LockRegistry lockRegistry;

	@Autowired
	JdbcSchedulerLockConfig jdbcSchedulerLockConfig;

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
		lockRegistry.obtain(TestScheduledJob.TEST_LOCK).lock();
		Thread.sleep(5000);
		assertThat(testScheduledJob.getInvocationCount()).isEqualTo(0);
	}

	@Test
	public void schedulerShouldConfigureLockFromAnnotationMetaData() {

		assertThat(jdbcSchedulerLockConfig.getTablePrefix()).isEqualTo("INT_");
		assertThat(jdbcSchedulerLockConfig.getRegion()).isEqualTo("dsf");
		assertThat(jdbcSchedulerLockConfig.getTimeToLive()).isEqualTo(10000);
	}

	@Configuration
	@EnableScheduling
	@EnableJdbcSchedulerLocking(tablePrefix = "${tablePrefix}", region = "${region}", timeToLive = 10000)
	public static class TestConfig {

		@Bean
		public TestScheduledJob testScheduledJob() {
			return new TestScheduledJob();
		}
	}

	@Data
	public static class TestScheduledJob {

		public static final String TEST_LOCK = "testLock";
		int invocationCount;

		@Scheduled(fixedDelay = 3000, initialDelay = 2000)
		@SchedulerLock(name = TEST_LOCK, lockRegistryBean = "lockRegistry")
		public void runJob() {
			invocationCount++;
		}

		public void resetInvocationCount() {
			this.invocationCount = 0;
		}
	}

}
