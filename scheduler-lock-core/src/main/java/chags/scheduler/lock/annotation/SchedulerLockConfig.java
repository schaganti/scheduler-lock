package chags.scheduler.lock.annotation;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringValueResolver;

import chags.scheduler.lock.LockingTaskScheduler;
import chags.scheduler.lock.SynchronizedRunnableMapper;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SchedulerLockConfig implements EmbeddedValueResolverAware {

	private StringValueResolver stringValueResolver;

	@Bean(name = { "taskScheduler" })
	TaskScheduler taskScheduler(SynchronizedRunnableMapper synchronizedRunnerMapper, Environment env) {

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(Integer.parseInt(env.getProperty("scheduler-lock.thread-pool-size", "10")));
		threadPoolTaskScheduler.initialize();
		log.info("Creating LockingTaskScheduler bean");
		return new LockingTaskScheduler(threadPoolTaskScheduler, synchronizedRunnerMapper);
	}

	@Bean
	public SynchronizedRunnableMapper synchronizedRunnerMapper(LockRegistry lockRegistry,
			ApplicationContext applicationContext) {
		log.info("Creating SynchronizedRunnableMapper bean with default lock registry: {}", lockRegistry);
		return new SynchronizedRunnableMapper(lockRegistry, applicationContext);
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.stringValueResolver = resolver;
	}

	protected StringValueResolver getStringValueResolver() {
		return this.stringValueResolver;
	}

	protected AnnotationAttributes getAnnotationAttributes(AnnotationMetadata importMetadata, String className) {

		Map<String, Object> attributeMap = importMetadata.getAnnotationAttributes(className);
		return AnnotationAttributes.fromMap(attributeMap);
	}
}
