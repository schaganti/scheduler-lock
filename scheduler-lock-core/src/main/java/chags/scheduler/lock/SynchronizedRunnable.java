package chags.scheduler.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.StringUtils;

import chags.scheduler.lock.annotation.SchedulerLock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter(value = AccessLevel.PACKAGE)
@ToString
@Slf4j
public class SynchronizedRunnable implements Runnable {

	private ScheduledMethodRunnable runnable;
	private long maxWaitTime;
	private String lockName;
	private LockRegistry lockRegistry;

	@Override
	public void run() {

		Lock lock = lockRegistry.obtain(lockName);
		try {
			log.info("Trying to aquire lock: {}", lockName);
			if (acquireLock(lock)) {
				try {
					log.info("Aquired lock: {}, running: {}()", lockName, runnable);
					runnable.run();
				} finally {
					log.info("Releasing lock: {}", lockName);
					lock.unlock();
				}
			} else {
				log.info("Could not aquire lock: {}, skipping execution", lockName);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Exception occured while trying to acquire the lock " + e.getMessage(), e);
		}
	}

	private boolean acquireLock(Lock lock) throws InterruptedException {
		return lock.tryLock(maxWaitTime, TimeUnit.MILLISECONDS);
	}
}
