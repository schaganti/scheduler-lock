package chags.scheduler.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

@RunWith(MockitoJUnitRunner.class)
public class LockingTaskSchedulerTest {

	LockingTaskScheduler subject;

	@Mock
	TaskScheduler delegate;
	
	@Mock
	LockRegistry defaultLockRegistry;

	@Mock
	ScheduledMethodRunnable runnable;
	
	@Mock
	SynchronizedRunnable syncRunnable;
	
	@Mock
	SynchronizedRunnableMapper synchRunnableMapper;

	@Captor
	ArgumentCaptor<Runnable> runnableCaptor;
	
	@Before 
	public void before() 
	{
		subject = new LockingTaskScheduler(delegate, synchRunnableMapper);
		when(synchRunnableMapper.apply(any(Runnable.class))).thenReturn(syncRunnable);
	}

	@Test
	public void shouldWrapRunnableAndSchedule() {
		
		Date startTime = new Date();
		Trigger trigger = mock(Trigger.class);

		subject.schedule(runnable, startTime);
		subject.schedule(runnable, trigger);
		
		subject.scheduleAtFixedRate(runnable, 100);
		subject.scheduleAtFixedRate(runnable, startTime, 100);

		subject.scheduleWithFixedDelay(runnable, 100);
		subject.scheduleWithFixedDelay(runnable, startTime, 100);

		
		verify(delegate).schedule(runnableCaptor.capture(), same(startTime));
		verify(delegate).schedule(runnableCaptor.capture(),same(trigger));
		
		verify(delegate).scheduleAtFixedRate(runnableCaptor.capture(),same(100L));
		verify(delegate).scheduleAtFixedRate(runnableCaptor.capture(),same(startTime), same(100L));
		
		verify(delegate).scheduleWithFixedDelay(runnableCaptor.capture(),same(100L));
		verify(delegate).scheduleWithFixedDelay(runnableCaptor.capture(),same(startTime), same(100L));
		
		runnableCaptor.getAllValues().forEach(v -> assertThat(v).isEqualTo(syncRunnable));
	}
}
