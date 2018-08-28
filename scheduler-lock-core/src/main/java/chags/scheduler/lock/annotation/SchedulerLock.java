package chags.scheduler.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerLock {

	String name() default "";
	long maxWaitTime() default 0;
	String lockRegistryBean() default "";
}
