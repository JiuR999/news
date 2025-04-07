import com.swust.common.redis.CacheService;
import com.swust.schedule.ScheduleApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RunWith(SpringRunner.class)
public class RedisTest {
    private static final String LOCK_KEY = "my_lock"; // 锁的标识符
    private static final int LOCK_EXPIRE_TIME_MS = 4000;
    @Autowired
    CacheService cacheService;

    @Test
    public void testListPush() {
        for (int i = 0; i < 10; i++) {
            cacheService.lLeftPush("test", "test"+i);
        }

    }

    @Test
    public void testListPpo() {
        Long len = cacheService.lLen("test");
        System.out.println("一共有"+len+"数据");
        for (int i = 0; i < len; i++) {
            System.out.println(cacheService.lRightPop("test"));
        }
    }

    @Test
    public void testSetNx() {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        AtomicInteger successfulLocks = new AtomicInteger(5); // 记录成功加锁的次数
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "test-lock-thread-" + atomicInteger.getAndIncrement());
            return thread;
        };
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                100,
                200,
                1000,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                threadFactory
        );
        Runnable command = () -> {
            log.info("{} 开始尝试抢单", Thread.currentThread().getName());
            while (successfulLocks.get() > 0) {
                String token = tryLock();
                if (token != null) {
                    log.info("{} 抢单成功", Thread.currentThread().getName());
                    log.info(token);
                    successfulLocks.decrementAndGet();
                    // 模拟业务逻辑处理
                    try {
                        Thread.sleep(2000); // 模拟业务处理时间
                    } catch (InterruptedException e) {
                        log.error("Thread interrupted", e);
                    }
                    log.info("{} 释放锁", Thread.currentThread().getName());
                    releaseLock(token);
                    return;
                } else {
                    log.info("{} 抢单失败", Thread.currentThread().getName());
                }
                try {
                    Thread.sleep(100); // 模拟线程间切换时间
                } catch (InterruptedException e) {
                    log.error("Thread interrupted", e);
                }
            }
        };

        // 提交多个任务以测试并发锁行为
        for (int i = 0; i < 1000; i++) {
            poolExecutor.execute(command);
        }

        poolExecutor.shutdown();
        try {
            poolExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("ThreadPoolExecutor interrupted", e);
        }

        log.info("还剩{}库存", successfulLocks.get());
    }
    private void releaseLock(String token) {
        RedisConnectionFactory factory = cacheService.getstringRedisTemplate().getConnectionFactory();
        RedisConnection connection = null;

        try {
            connection = factory.getConnection();
            byte[] lockKeyBytes = LOCK_KEY.getBytes();
            byte[] lockValueBytes = token.getBytes();

            // 使用 Lua 脚本确保原子性释放锁
            String luaScript = "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";
            Object result = connection.eval(luaScript.getBytes(), ReturnType.INTEGER, 1, lockKeyBytes, lockValueBytes);
            log.info("Excute Res:{}", result);
            if ("1".equals(String.valueOf(result))) {
                log.info("Lock released successfully");
            } else {
                log.warn("Failed to release lock, it may have been already released or not owned by this client");
            }
        } catch (Exception e) {
            log.error("Failed to release lock", e);
            throw new RuntimeException("Failed to release lock", e);
        } finally {
            RedisConnectionUtils.releaseConnection(connection, factory, false);
        }
    }
    private String tryLock() {
        RedisConnectionFactory factory = cacheService.getstringRedisTemplate().getConnectionFactory();
        RedisConnection connection = factory.getConnection();
        String token = UUID.randomUUID().toString();
        try {
            Boolean locked = connection.set(
                    LOCK_KEY.getBytes(),
                    token.getBytes(),
                    Expiration.from(4000, TimeUnit.MILLISECONDS),
                    RedisStringCommands.SetOption.SET_IF_ABSENT
            );
            if (locked != null && locked) {
                return token;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            //释放锁
            RedisConnectionUtils.releaseConnection(connection, factory, false);
        }
        return null;
    }
}
