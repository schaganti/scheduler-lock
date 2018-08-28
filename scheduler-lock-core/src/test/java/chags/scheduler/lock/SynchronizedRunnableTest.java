package chags.scheduler.lock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import chags.scheduler.lock.annotation.SchedulerLock;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedRunnableTest {

	private String lockName = "lockName";
	
	private long maxWaitTime = 1212;
	
	private SynchronizedRunnable subject;

	@Mock
	private ScheduledMethodRunnable delegate;
	
	@Mock
	private ScheduledMethodRunnable scheduledMethodRunnable;
	
	@Mock
	private LockRegistry defaultLockRegistry;
	
	@Mock
	private Lock lock;

	@Before
	public void before() {
		when(defaultLockRegistry.obtain(lockName)).thenReturn(lock);
		subject = new SynchronizedRunnable(delegate, maxWaitTime, lockName, defaultLockRegistry);
	}
	
//	@Test
//	public void defaultLockNameToClassNameAndMethodName() {
//		
//		when(schedulerLock.name()).thenReturn("");
//		Method mockMethod = mock(Method.class);
//		when(scheduledMethodRunnable.getMethod()).thenReturn(mockMethod);
//		Object mockObject = mock(Object.class);
//		when(scheduledMethodRunnable.getTarget()).thenReturn(mockObject);
//		subject.run();
//		verify(defaultLockRegistry).obtain(Object.class.getName());
//	}

	@Test
	public void shouldAcquireLockAndCallDelegate() throws NoSuchMethodException, SecurityException, InterruptedException {

		when(lock.tryLock(maxWaitTime, TimeUnit.MILLISECONDS)).thenReturn(true);
		subject.run();
		InOrder inOrder = inOrder(defaultLockRegistry, delegate, lock);
		inOrder.verify(defaultLockRegistry).obtain(lockName);
		inOrder.verify(lock).tryLock(maxWaitTime, TimeUnit.MILLISECONDS);
		inOrder.verify(delegate).run();
		inOrder.verify(lock).unlock();
	}
	
	@Test
	public void shouldSkipCallingDelegateWithoutLock() throws NoSuchMethodException, SecurityException, InterruptedException {

		when(lock.tryLock(maxWaitTime, TimeUnit.MILLISECONDS)).thenReturn(false);
		subject.run();
		InOrder inOrder = inOrder(defaultLockRegistry, delegate, lock);
		inOrder.verify(defaultLockRegistry).obtain(lockName);
		inOrder.verify(lock).tryLock(maxWaitTime, TimeUnit.MILLISECONDS);
		verifyZeroInteractions(delegate);
	}
}
