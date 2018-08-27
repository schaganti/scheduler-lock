package chags.scheduler.lock.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(RedisSchedulerLockConfig.class)
public @interface EnableRedisSchedulerLocking {

	String redisNameSpace() default "schedulerLock";
	
}
