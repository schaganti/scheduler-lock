package chags.scheduler.lock.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(JdbcSchedulerLockConfig.class)
public @interface EnableJdbcSchedulerLocking {

	String tablePrefix() default "INT_";

	String region() default "";
	
	int timeToLive() default 10000;
}
