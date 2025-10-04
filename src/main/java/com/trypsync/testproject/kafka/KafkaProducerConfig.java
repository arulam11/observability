package com.trypsync.testproject.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {
  @Bean
  public ProducerFactory<String, String> producerFactory(KafkaProperties props,
      MeterRegistry registry) {
    Map<String, Object> cfg = props.buildProducerProperties();
    cfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    cfg.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
    cfg.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    DefaultKafkaProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(cfg);

    // 🔗 Bind Micrometer metrics to each producer the factory creates
    pf.addListener(new ProducerFactory.Listener<String, String>() {
      @Override
      public void producerAdded(String id, Producer<String, String> producer) {
        new KafkaClientMetrics(producer).bindTo(registry);
      }
    });

    return pf;    

  }

   @Bean
  public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
    KafkaTemplate<String, String> t = new KafkaTemplate<>(pf);
    try { t.setObservationEnabled(true); } catch (NoSuchMethodError ignored) {}
    return t;
  }


}
