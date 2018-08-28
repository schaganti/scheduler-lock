package chags.scheduler.lock;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class LockingTaskScheduler implements TaskScheduler {

	private TaskScheduler delegate;

	private Function<Runnable, Runnable> synchronizedRunnableMapper;

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return delegate.schedule(wrap(task), trigger);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return delegate.schedule(wrap(task), startTime);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return delegate.scheduleAtFixedRate(wrap(task), startTime, period);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return delegate.scheduleAtFixedRate(wrap(task), period);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return delegate.scheduleWithFixedDelay(wrap(task), startTime, delay);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return delegate.scheduleWithFixedDelay(wrap(task), delay);
	}

	Runnable wrap(Runnable r) {
		return synchronizedRunnableMapper.apply(r);
	}

}
