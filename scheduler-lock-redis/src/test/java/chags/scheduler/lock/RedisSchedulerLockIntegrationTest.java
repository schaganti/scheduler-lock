package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.locks.Lock;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import chags.scheduler.lock.redis.annotation.EnableRedisSchedulerLocking;
import chags.scheduler.lock.redis.annotation.RedisSchedulerLockConfig;
import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { RedisSchedulerLockIntegrationTest.TestConfig.class })
@TestPropertySource(properties={"redisNameSpace=testNameSpace"})
public class RedisSchedulerLockIntegrationTest {

	private static final String REDIS_NAMEPSPACE = "testNameSpace";

	private static final int LOCK_EXPIRY_INTERVAL = 3600000;

	@Autowired
	TestScheduledJob testScheduledJob;

	@Autowired
	LockRegistry lockRegistry;
	
	@Autowired
	RedisSchedulerLockConfig redisSchedulerLockConfig;
	
	@Autowired
	StringRedisTemplate redisTemplate;

	@BeforeClass
	public static void beforeClass() {
		TestRedisServerUtil.start();
	}

	@AfterClass
	public static void afterClass() {
		TestRedisServerUtil.stop();
	}

	@Before
	public void before() {
		testScheduledJob.resetInvocationCounter();
	}

	@Test
	public void schedulerShouldRunTheJob() throws InterruptedException {
		Thread.sleep(5000);
		assertThat(testScheduledJob.getInvocationCount()).isGreaterThan(0);
	}

	@Test
	public void schedulerShouldSkipTheJob() throws InterruptedException {

		// We need a different redis connection to test locking scenario - with
		// the same connection/client the lock is re-entrant.
		// This simulates the connection and loc from a different jvm
		Lock lock = newLockRegistry().obtain(TestScheduledJob.TEST_LOCK);
		try {
			boolean locked = lock.tryLock();
			assertThat(locked).isEqualTo(true);
			Thread.sleep(5000);
			assertThat(testScheduledJob.getInvocationCount()).isEqualTo(0);
		} finally {
			lock.unlock();
		}
	}

	@Test
	public void schedulerShouldConfigureLockFromAnnotationMetaData() {
		
		assertThat(redisSchedulerLockConfig.getRedisNameSpace()).isEqualTo(REDIS_NAMEPSPACE);
		assertThat(redisSchedulerLockConfig.getLockExipryInterval()).isEqualTo(LOCK_EXPIRY_INTERVAL);
		
		String testLockKey = "testLock123";
		
		Lock lock = lockRegistry.obtain(testLockKey);
		try {
			assertThat(lock.tryLock()).isEqualTo(true);
			assertThat(redisTemplate.keys(REDIS_NAMEPSPACE+":"+testLockKey)).isNotEmpty();
		} finally {
			lock.unlock();
		}
	}

	public LockRegistry newLockRegistry() {
		RedisLockRegistry redisLockRegistry = new RedisLockRegistry(TestRedisServerUtil.newRedisConnectionFactory(),
				REDIS_NAMEPSPACE);
		return redisLockRegistry;
	}

	@Configuration
	@EnableScheduling
	@EnableRedisSchedulerLocking(redisNameSpace ="${redisNameSpace}", lockExpiryInterval = LOCK_EXPIRY_INTERVAL)
	public static class TestConfig {

		@Bean
		public RedisConnectionFactory redisConnectionFactory() {
			return new LettuceConnectionFactory("localhost", TestRedisServerUtil.port);
		}
		
		@Bean
		public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
			StringRedisTemplate redisTemplate = new StringRedisTemplate();
			redisTemplate.setConnectionFactory(redisConnectionFactory);
			return redisTemplate;
		}

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
		@SchedulerLock(name = TEST_LOCK)
		public void runJob() {
			invocationCount++;
		}

		public void resetInvocationCounter() {
			invocationCount = 0;
		}
	}
}
