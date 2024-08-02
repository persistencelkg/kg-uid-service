package org.lkg.controller;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.baidu.fsg.uid.utils.NamingThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.concurrent.*;

/**
 * Description:
 * Author: 李开广
 * Date: 2024/8/2 3:43 PM
 */
@RestController
@RequestMapping("/id")
@Slf4j
public class IdGenerateController implements DisposableBean {

    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors() >> 1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(CORE_SIZE, new NamingThreadFactory("batch-get"));

    @Resource
    private DefaultUidGenerator defaultUidGenerator;

    @Value("${batch-get-num-max:500}")
    private Integer batchGetNumMax;

    @GetMapping("/get")
    public long getId() {
        return defaultUidGenerator.getUID();
    }

    @GetMapping("/batch-get/{num}")
    public HashSet<Long> getId(@PathVariable("num") int num) {
        if (num > batchGetNumMax) {
            throw new IllegalArgumentException("max support batch get size:" + batchGetNumMax);
        }
        int batchCount = num / CORE_SIZE;
        int lastBatchCount = num % CORE_SIZE == 0 ? batchCount : batchCount + (num % CORE_SIZE);
        HashSet<Long> objects = new HashSet<>(num);
        for (int i = 0; i < CORE_SIZE; i++) {
            if (i == CORE_SIZE - 1) {
                batchCount = lastBatchCount;
            }
            int finalBatchCount = batchCount;
            Future<?> submit = executorService.submit(() -> {
                for (int j = 0; j < finalBatchCount; j++) {
                    objects.add(defaultUidGenerator.getUID());
                }
            });
            try {
                submit.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("batch-get ids:{}", objects.size());
        return objects;
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
    }
}
