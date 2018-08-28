package chags.scheduler.lock;

import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.StringUtils;

import chags.scheduler.lock.annotation.SchedulerLock;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SynchronizedRunnableMapper implements Function<Runnable, Runnable> {

	private LockRegistry defaultLockRegistry;
	private ApplicationContext applicationContext;

	@Override
	public Runnable apply(Runnable runnable) {

		log.info("Received instance for wrapping {}", runnable);

		if (!(runnable instanceof ScheduledMethodRunnable)) {
			log.info("Not wrapping {} into Synchronized Runnable as its not of type ScheduledMethodRunnable",
					runnable);
			return runnable;
		}

		ScheduledMethodRunnable scheduledRunnerForMethod = (ScheduledMethodRunnable) runnable;
		SchedulerLock schedulerLock = AnnotatedElementUtils.getMergedAnnotation(scheduledRunnerForMethod.getMethod(),
				SchedulerLock.class);

		if (schedulerLock == null) {
			log.info("SchedulerLock annotation not present on {}, not wrapping SyncrhonizedRunner",
					getFullyQualifiedMethodName(scheduledRunnerForMethod));
			return runnable;
		}

		SynchronizedRunnable synchronizedRunnable = new SynchronizedRunnable(scheduledRunnerForMethod,
				schedulerLock.maxWaitTime(), resolveLockName(scheduledRunnerForMethod, schedulerLock),
				getLockRegistry(schedulerLock));

		log.info("wrapping runnable {} into {}", runnable, synchronizedRunnable);

		return synchronizedRunnable;
	}

	private String resolveLockName(ScheduledMethodRunnable runnableMethod, SchedulerLock schedulerLock) {
		return StringUtils.isEmpty(schedulerLock.name()) ? getFullyQualifiedMethodName(runnableMethod)
				: schedulerLock.name();
	}

	private String getFullyQualifiedMethodName(ScheduledMethodRunnable runnableMethod) {
		return runnableMethod.getTarget().getClass().getName() + "." + runnableMethod.getMethod().getName();
	}

	private LockRegistry getLockRegistry(SchedulerLock schedulerLock) {
		String lockRegistryBeanName = schedulerLock.lockRegistryBean();
		LockRegistry lockRegistryToUse = StringUtils.isEmpty(lockRegistryBeanName) ? defaultLockRegistry
				: applicationContext.getBean(lockRegistryBeanName, LockRegistry.class);
		return lockRegistryToUse;
	}
}
