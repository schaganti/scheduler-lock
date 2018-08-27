package chags.scheduler.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerLock {

	String name();
	long maxWaitTime() default 0;
	String lockRegistryBean() default "";
}
