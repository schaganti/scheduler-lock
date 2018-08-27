package chags.scheduler.lock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedRunnableTest {

	private String lockName = "lockName";
	
	private long maxWaitTime = 1212;
	
	private SynchronizedRunnable subject;

	@Mock
	private Runnable delegate;
	
	@Mock
	private ScheduledMethodRunnable scheduledMethodRunnable;
	
	@Mock
	private LockRegistry defaultLockRegistry;
	
	@Mock
	private SchedulerLock schedulerLock;
	
	@Mock
	private Lock lock;

	@Before
	public void before() {
		when(schedulerLock.name()).thenReturn(lockName);
		when(schedulerLock.maxWaitTime()).thenReturn(maxWaitTime);
		when(defaultLockRegistry.obtain(lockName)).thenReturn(lock);
		subject = new SynchronizedRunnable(delegate, schedulerLock, defaultLockRegistry);
	}

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
