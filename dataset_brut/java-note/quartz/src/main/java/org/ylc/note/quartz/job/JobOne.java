package org.ylc.note.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 代码全万行，注释第一行
 * 注释不规范，同事泪两行
 *
 * @author YuLc
 * @version 1.0.0
 * @date 2020-09-15
 */
@Slf4j
@Component
public class JobOne {

    public void run() {
        log.info("job1 -> 50秒执行一次，当前时间：[{}]", LocalDateTime.now().toString());
    }
}
