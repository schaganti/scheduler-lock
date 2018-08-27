package chags.scheduler.lock.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(SchedulerLockConfig.class)
public @interface EnableSchedulerLocking {

}
