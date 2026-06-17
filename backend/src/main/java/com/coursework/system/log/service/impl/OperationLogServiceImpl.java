package com.coursework.system.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursework.system.log.entity.OperationLog;
import com.coursework.system.log.mapper.OperationLogMapper;
import com.coursework.system.log.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void record(Long userId, String username, String operation, String targetType, Long targetId,
                       String ip, String result, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setIp(ip);
        log.setResult(result);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        save(log);
    }

    @Override
    public List<OperationLog> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return list(new QueryWrapper<OperationLog>().orderByDesc("created_at").last("LIMIT " + safeLimit));
    }
}
