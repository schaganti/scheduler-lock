package chags.scheduler.lock;

import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.StringUtils;

import chags.scheduler.lock.annotation.SchedulerLock;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SynchronizedRunnableMapper implements Function<Runnable, Runnable> {

	private LockRegistry defaultLockRegistry;
	private ApplicationContext applicationContext;

	@Override
	public Runnable apply(Runnable runnable) {

		if (!(runnable instanceof ScheduledMethodRunnable)) {
			return runnable;
		}

		ScheduledMethodRunnable scheduledRunnerForMethod = (ScheduledMethodRunnable) runnable;
		SchedulerLock schedulerLock = AnnotatedElementUtils.getMergedAnnotation(scheduledRunnerForMethod.getMethod(),
				SchedulerLock.class);

		if (schedulerLock == null) {
			return runnable;
		}

		return new SynchronizedRunnable(scheduledRunnerForMethod, schedulerLock.maxWaitTime(),
				resolveLockName(scheduledRunnerForMethod, schedulerLock), getLockRegistry(schedulerLock));
	}

	private String resolveLockName(ScheduledMethodRunnable runnableMethod, SchedulerLock schedulerLock) {
		return StringUtils.isEmpty(schedulerLock.name())
				? runnableMethod.getTarget().getClass().getName() + ":" + runnableMethod.getMethod().getName()
				: schedulerLock.name();
	}

	private LockRegistry getLockRegistry(SchedulerLock schedulerLock) {
		String lockRegistryBeanName = schedulerLock.lockRegistryBean();
		LockRegistry lockRegistryToUse = StringUtils.isEmpty(lockRegistryBeanName) ? defaultLockRegistry
				: applicationContext.getBean(lockRegistryBeanName, LockRegistry.class);
		return lockRegistryToUse;
	}
}
