package chags.scheduler.lock.redis.annotation;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.util.StringValueResolver;

import chags.scheduler.lock.annotation.SchedulerLockConfig;
import lombok.Getter;

@Configuration
@Getter
public class RedisSchedulerLockConfig extends SchedulerLockConfig implements ImportAware, EmbeddedValueResolverAware {

	private String redisNameSpace;

	private int lockExipryInterval;

	@Bean
	public LockRegistry lockRegistry(RedisConnectionFactory redisConnectionFactory) {

		StringValueResolver stringValueResolver = getStringValueResolver();
		redisNameSpace = stringValueResolver.resolveStringValue(redisNameSpace);

		return new RedisLockRegistry(redisConnectionFactory, redisNameSpace, lockExipryInterval);
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		AnnotationAttributes attributes = getAnnotationAttributes(importMetadata,
				EnableRedisSchedulerLock.class.getName());
		this.redisNameSpace = attributes.getString("redisNameSpace");
		this.lockExipryInterval = attributes.getNumber("lockExpiryInterval");
	}
}
