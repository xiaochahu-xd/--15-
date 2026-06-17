package com.coursework.system.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coursework.system.log.entity.OperationLog;

import java.util.List;

public interface OperationLogService extends IService<OperationLog> {
    void record(Long userId, String username, String operation, String targetType, Long targetId,
                String ip, String result, String detail);

    List<OperationLog> latest(int limit);
}
