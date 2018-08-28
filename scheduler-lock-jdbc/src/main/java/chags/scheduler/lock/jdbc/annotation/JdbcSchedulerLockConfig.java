package chags.scheduler.lock.jdbc.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.util.StringValueResolver;

import chags.scheduler.lock.annotation.SchedulerLockConfig;
import lombok.Getter;

@Configuration
@Getter
public class JdbcSchedulerLockConfig extends SchedulerLockConfig implements ImportAware {

	private String tablePrefix;

	private String region;

	private int timeToLive;

	@Bean
	public LockRegistry lockRegistry() {
		
		StringValueResolver stringValueResolver = getStringValueResolver();
		this.tablePrefix = stringValueResolver.resolveStringValue(tablePrefix);
		this.region = stringValueResolver.resolveStringValue(region);
		return new DefaultLockRegistry();
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes annotationAttributes = getAnnotationAttributes(importMetadata,
				EnableJdbcSchedulerLocking.class.getName());
		this.tablePrefix = annotationAttributes.getString("tablePrefix");
		this.region = annotationAttributes.getString("region");
		this.timeToLive = annotationAttributes.getNumber("timeToLive");

	}
}
