package chags.scheduler.lock;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.SocketUtils;

import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

public class TestRedisServerUtil {

	static RedisServer redisServer;

	static RedisConnectionFactory redisConnectionFactory;

	static int port = SocketUtils.findAvailableTcpPort(1000, 5000);

	public static void start() {
		redisServer = new RedisServerBuilder().port(port).setting("maxmemory 128M").build();
		redisServer.start();
	}

	public static RedisConnectionFactory newRedisConnectionFactory() {

		LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory("localhost", port);
		lettuceConnectionFactory.afterPropertiesSet();
		return lettuceConnectionFactory;
	}

	public static void stop() {
		redisServer.stop();
	}

}
