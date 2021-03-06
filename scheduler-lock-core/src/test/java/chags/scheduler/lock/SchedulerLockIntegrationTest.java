package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import chags.scheduler.lock.annotation.EnableSchedulerLock;
import chags.scheduler.lock.annotation.SchedulerLock;
import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes={SchedulerLockIntegrationTest.TestConfig.class})
public class SchedulerLockIntegrationTest {

	@Autowired
	TestScheduledJob testScheduledJob;
	
	@Autowired
	LockRegistry lockRegistry;
	
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
	
	@Configuration
	@EnableScheduling
	@EnableSchedulerLock
	public static class TestConfig {

		@Bean
		public TestScheduledJob testScheduledJob() {
			return new TestScheduledJob();
		}
		
		@Bean 
		public LockRegistry lockRegistry() {
			return new DefaultLockRegistry();
		}
	}
	
	@Data
	public static class TestScheduledJob {
		
		public static final String TEST_LOCK = "testLock";
		int invocationCount;

		@Scheduled(fixedDelay=3000, initialDelay=2000)
		@SchedulerLock(name=TEST_LOCK, lockRegistryBean="lockRegistry", maxWaitTime=7000)
		public void runJob() {
			invocationCount++;
		}
		
		public void resetInvocationCount() {
			this.invocationCount = 0;
		}
	}
}
