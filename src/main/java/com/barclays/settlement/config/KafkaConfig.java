package com.barclays.settlement.config;

import com.barclays.settlement.event.MismatchEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

  @Bean
  public ProducerFactory<String, MismatchEvent> mismatchEventProducerFactory(
      KafkaProperties properties, SslBundles sslBundles) {
    var configs = properties.buildProducerProperties(sslBundles);
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    return new DefaultKafkaProducerFactory<>(configs);
  }

  @Bean
  public KafkaTemplate<String, MismatchEvent> mismatchEventKafkaTemplate(
      ProducerFactory<String, MismatchEvent> mismatchEventProducerFactory) {
    return new KafkaTemplate<>(mismatchEventProducerFactory);
  }
}
