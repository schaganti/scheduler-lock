package chags.scheduler.lock.annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import chags.scheduler.lock.LockingTaskScheduler;
import chags.scheduler.lock.SynchronizedRunnableMapper;

@Configuration
public class SchedulerLockConfig {
	
	@Bean
	TaskScheduler schedulerLockTaskScheduler(SynchronizedRunnableMapper synchronizedRunnerMapper) {

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(10);
		threadPoolTaskScheduler.initialize();
		return new LockingTaskScheduler(threadPoolTaskScheduler, synchronizedRunnerMapper);
	}

	@Bean
	public SynchronizedRunnableMapper synchronizedRunnerMapper(LockRegistry lockRegistry, ApplicationContext applicationContext) {
		return new SynchronizedRunnableMapper(lockRegistry, applicationContext);
	}
}
