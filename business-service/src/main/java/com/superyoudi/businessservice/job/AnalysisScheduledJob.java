package com.superyoudi.businessservice.job;

import com.superyoudi.businessservice.mq.AnalysisJobProducer;
import com.superyoudi.businessservice.mq.MqTopicConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 学情分析定时任务的<b>兜底恢复</b>。
 *
 * <p>主调度已改为 RocketMQ 定时消息"自续链"
 * （见 {@code AnalysisJobBootstrapRunner} 启动发首条 + {@code AnalysisJobConsumer} 执行后发下一条），
 * 本类不再直接执行 syncAll，只做兜底：定期检查每个任务的心跳，
 * 若消息链断开（心跳长时间未更新），则补发一条延时消息重新拉起。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisScheduledJob {

    private final StringRedisTemplate stringRedisTemplate;
    private final AnalysisJobProducer analysisJobProducer;

    /**
     * 每 5 分钟兜底扫描一次，检查三种分析任务的消息链是否断开。
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    public void recoverStaleJobs() {
        recoverJob(MqTopicConstants.JOB_TEACHER_COURSE, MqTopicConstants.JOB_TEACHER_COURSE_INTERVAL_SECONDS);
        recoverJob(MqTopicConstants.JOB_STUDENT_COURSE, MqTopicConstants.JOB_STUDENT_COURSE_INTERVAL_SECONDS);
        recoverJob(MqTopicConstants.JOB_MISSION, MqTopicConstants.JOB_MISSION_INTERVAL_SECONDS);
    }

    /**
     * 若某任务心跳超过 2 个周期未更新，则认为消息链断开，补发一条延时消息。
     */
    private void recoverJob(String jobType, long intervalSeconds) {
        String heartbeatKey = MqTopicConstants.REDIS_ANALYSIS_JOB_HEARTBEAT_PREFIX + jobType;
        String lastStr = stringRedisTemplate.opsForValue().get(heartbeatKey);
        long lastExecTime = lastStr == null ? 0L : Long.parseLong(lastStr);

        // 心跳未超时（未超过 2 个周期）→ 消息链正常，跳过
        if (System.currentTimeMillis() - lastExecTime <= 2L * intervalSeconds * 1000L) {
            return;
        }

        // SETNX 短锁：防止多实例同时补发导致多条消息链
        String lockKey = MqTopicConstants.REDIS_ANALYSIS_JOB_RECOVER_LOCK_PREFIX + jobType;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 60L, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            log.warn("分析任务消息链疑似断开，补发调度, jobType={}, 上次执行时间戳={}", jobType, lastExecTime);
            analysisJobProducer.sendDelayed(jobType, intervalSeconds);
            // 锁 TTL 60 秒自动释放，无需手动删除
        }
    }
}
