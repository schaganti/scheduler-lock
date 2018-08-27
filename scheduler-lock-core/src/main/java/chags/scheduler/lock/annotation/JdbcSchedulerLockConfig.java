package chags.scheduler.lock.annotation;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

@Configuration
public class JdbcSchedulerLockConfig extends SchedulerLockConfig implements ImportAware {

	@Bean
	public LockRegistry lockRegistry() {
		return new DefaultLockRegistry();
	}
	
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		Map<String, Object> attributeMap = importMetadata
				.getAnnotationAttributes(EnableJdbcSchedulerLocking.class.getName());
		
		System.out.println(attributeMap);

		AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributeMap);
	}

}
