package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.rowset.spi.SyncResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import lombok.Data;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedRunnableMapperTest {

	SynchronizedRunnableMapper subject;

	@Mock
	LockRegistry defaultLockRegistry;
	
	@Mock
	ApplicationContext applicationContext;

	@Before
	public void before() {
		subject = new SynchronizedRunnableMapper(defaultLockRegistry, applicationContext);
	}
	
	@Test
	public void shouldMapToSynchronizedRunnable() throws NoSuchMethodException, SecurityException {
	
		TestTaskBean testTaskBean = new TestTaskBean();
		ScheduledMethodRunnable scheduledRunnerForMethod = createScheduledRunnerForMethod(testTaskBean, "runWithLock");
		
		Runnable synchRunner = subject.apply(scheduledRunnerForMethod);
		
		assertThat(synchRunner).isInstanceOf(SynchronizedRunnable.class);
	}
	
	@Test
	public void shouldNotMapToSychronizedRunnableForMethodsWithoutAnnotation() throws NoSuchMethodException {

		TestTaskBean testTaskBean = new TestTaskBean();
		ScheduledMethodRunnable scheduledRunnerForMethod = createScheduledRunnerForMethod(testTaskBean, "run");
		
		Runnable synchRunner = subject.apply(scheduledRunnerForMethod);
		
		assertThat(synchRunner).isNotInstanceOf(SynchronizedRunnable.class);
		assertThat(synchRunner).isInstanceOf(ScheduledMethodRunnable.class);
	}
	
	@Test
    public void shouldConstructSynchronizedRunnerWithSchedulerLockAndDefaultLockRegistry() throws NoSuchMethodException {

		TestTaskBean testTaskBean = new TestTaskBean();
		ScheduledMethodRunnable scheduledRunnerForMethod = createScheduledRunnerForMethod(testTaskBean, "runWithLock");
		
		SynchronizedRunnable synchRunner = (SynchronizedRunnable) subject.apply(scheduledRunnerForMethod);
		
		assertThat(synchRunner.getSchedulerLock().name()).isEqualTo(TestTaskBean.MY_LOCK_NAME);
		assertThat(synchRunner.getLockRegistry()).isEqualTo(defaultLockRegistry);
	}
	
	@Test
	public void shouldRetrieveLockRegistryMyNameFromApplicationContext() throws NoSuchMethodException {

		TestTaskBean testTaskBean = new TestTaskBean();
		
		LockRegistry customLockRegistry = mock(LockRegistry.class);
		when(applicationContext.getBean("customLockRegistryBean", LockRegistry.class)).thenReturn(customLockRegistry);
		
		ScheduledMethodRunnable scheduledRunnerForMethod = createScheduledRunnerForMethod(testTaskBean, "runWithCustomLockRegistry");
		
		SynchronizedRunnable synchRunner = (SynchronizedRunnable) subject.apply(scheduledRunnerForMethod);

		assertThat(synchRunner.getLockRegistry()).isEqualTo(customLockRegistry);
	}
	
	private ScheduledMethodRunnable createScheduledRunnerForMethod(TestTaskBean bean, String methodName) throws NoSuchMethodException {
		TestTaskBean testBean = new TestTaskBean();
		return new ScheduledMethodRunnable(testBean, testBean.getClass().getMethod(methodName, null));
	}
	
	@Data
	public static class TestTaskBean {

		public static final String MY_LOCK_NAME = "myLockName";
		boolean invoked;
		
		@Scheduled(fixedDelay=1000)
		public void run() {
			invoked = true;
		}

		@Scheduled(fixedDelay=1000)
		@SchedulerLock(name=MY_LOCK_NAME)
		public void runWithLock() {
			invoked = true;
		}
		
		@Scheduled(fixedDelay=1000)
		@SchedulerLock(name=MY_LOCK_NAME, lockRegistryBean="customLockRegistryBean")
		public void runWithCustomLockRegistry() {
			invoked = true;
		}

	}


}
