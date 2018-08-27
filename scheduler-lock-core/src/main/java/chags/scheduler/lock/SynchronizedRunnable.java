package chags.scheduler.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(value = AccessLevel.PACKAGE)
public class SynchronizedRunnable implements Runnable {

	private Runnable runnable;
	private SchedulerLock schedulerLock;
	private LockRegistry lockRegistry;

	@Override
	public void run() {

		Lock lock = lockRegistry.obtain(schedulerLock.name());

		try {
			if (acquireLock(lock)) {
				try {
					runnable.run();
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Exception occured while trying to acquire the lock " + e.getMessage(), e);
		}
	}

	private boolean acquireLock(Lock lock) throws InterruptedException {
		if (schedulerLock.maxWaitTime() > 0) {
			return lock.tryLock(schedulerLock.maxWaitTime(), TimeUnit.MILLISECONDS);
		} else {
			return lock.tryLock();
		}
	}
}
