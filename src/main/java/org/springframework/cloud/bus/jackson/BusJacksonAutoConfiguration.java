/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.bus.jackson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.ConditionalOnBusEnabled;
import org.springframework.cloud.bus.endpoint.RefreshBusEndpoint;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.bus.event.UnknownRemoteApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

/**
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Donovan Muller
 * @author Stefan Pfeiffer
 */
@Configuration
@ConditionalOnBusEnabled
@ConditionalOnClass({ RefreshBusEndpoint.class, ObjectMapper.class })
@AutoConfigureAfter(BusAutoConfiguration.class)
public class BusJacksonAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(BusJacksonAutoConfiguration.class);

  @Bean
  @ConditionalOnMissingBean(name = "busJsonConverter")
  public BusJacksonMessageConverter busJsonConverter() {
    logger.info("busJsonConverter");
    return new BusJacksonMessageConverter();
  }

}

class BusJacksonMessageConverter extends AbstractMessageConverter
    implements InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(BusJacksonMessageConverter.class);

  private static final String DEFAULT_PACKAGE = ClassUtils
      .getPackageName(RemoteApplicationEvent.class);

  private final ObjectMapper mapper = new ObjectMapper();
  // private final ObjectMapper mapper = new Jackson2ObjectMapperBuilder().json().build();

  private String[] packagesToScan = new String[]{ DEFAULT_PACKAGE };

  public BusJacksonMessageConverter() {
    super(MimeTypeUtils.APPLICATION_JSON);
  }

  public void setPackagesToScan(String[] packagesToScan) {
    List<String> packages = new ArrayList<>(Arrays.asList(packagesToScan));
    if (!packages.contains(DEFAULT_PACKAGE)) {
      packages.add(DEFAULT_PACKAGE);
    }
    this.packagesToScan = packages.toArray(new String[0]);
  }

  private Class<?>[] findSubTypes() {
    List<Class<?>> types = new ArrayList<>();
    if (this.packagesToScan != null) {
      for (String pkg : this.packagesToScan) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
            false);
        provider.addIncludeFilter(
            new AssignableTypeFilter(RemoteApplicationEvent.class));

        Set<BeanDefinition> components = provider.findCandidateComponents(pkg);
        for (BeanDefinition component : components) {
          try {
            types.add(Class.forName(component.getBeanClassName()));
          } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "Failed to scan classpath for remote event classes", e);
          }
        }
      }
    }
    return types.toArray(new Class<?>[0]);
  }

  @Override
  protected boolean supports(Class<?> aClass) {
    // This converter applies only to RemoteApplicationEvent and subclasses
    return RemoteApplicationEvent.class.isAssignableFrom(aClass);
  }

  @Override
  public Object convertFromInternal(Message<?> message, Class<?> targetClass,
                                    Object conversionHint) {
    Object result = null;
    try {
      Object payload = message.getPayload();

      if (payload instanceof byte[]) {
        try {
          result = this.mapper.readValue((byte[]) payload, targetClass);
        } catch (InvalidTypeIdException e) {
          return new UnknownRemoteApplicationEvent(new Object(), e.getTypeId(), (byte[]) payload);
        }
      } else if (payload instanceof String) {
        try {
          result = this.mapper.readValue((String) payload, targetClass);
        } catch (InvalidTypeIdException e) {
          return new UnknownRemoteApplicationEvent(new Object(), e.getTypeId(), ((String) payload).getBytes());
        }
      }
    } catch (Exception e) {
      this.logger.error(e.getMessage(), e);
      return null;
    }
    logger.info("message payload is {}: '{}'", message.getPayload().getClass().getName(), message.getPayload());
    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.mapper.findAndRegisterModules();
    this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    this.mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
    this.mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    this.mapper.registerModule(new SubtypeModule(findSubTypes()));
  }
}
