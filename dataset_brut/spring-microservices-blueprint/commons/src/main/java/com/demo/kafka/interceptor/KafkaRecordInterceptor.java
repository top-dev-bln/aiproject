package com.demo.kafka.interceptor;

import org.apache.commons.lang3.StringUtils;
import com.demo.constants.CorrelationConstants;
import com.demo.util.CorrelationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Slf4j
public class KafkaRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> consumerRecord, Consumer<K, V> consumer) {
        String correlationId = null;
        Header header = consumerRecord.headers().lastHeader(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue());
        if (header != null && header.value() != null) {
            correlationId = new String(header.value(), StandardCharsets.UTF_8);
        }
        if (StringUtils.isBlank(correlationId)) {
            correlationId = CorrelationUtils.generateCorrelationId();
        }
        MDC.put(CorrelationConstants.CONTEXT_CORRELATION_ID.getValue(), correlationId);

        String payloadStr = String.valueOf(consumerRecord.value());
        log.info("Kafka message intercepted: topic={}, partition={}, offset={}, key={}, payload={}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
                consumerRecord.key(), payloadStr);

        return consumerRecord;
    }

}
