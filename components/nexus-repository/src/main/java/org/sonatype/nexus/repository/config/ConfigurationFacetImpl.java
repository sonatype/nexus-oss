/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.config;

import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.FacetSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link ConfigurationFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class ConfigurationFacetImpl
    extends FacetSupport
    implements ConfigurationFacet
{
  private final ConfigurationStore store;

  private final ObjectMapper objectMapper;

  private final ValidatorFactory validatorFactory;

  @Inject
  public ConfigurationFacetImpl(final ConfigurationStore store,
                                final @Named(ConfigurationObjectMapperProvider.NAME) ObjectMapper objectMapper,
                                final ValidatorFactory validatorFactory)
  {
    this.store = checkNotNull(store);
    this.objectMapper = checkNotNull(objectMapper);
    this.validatorFactory = checkNotNull(validatorFactory);
  }

  @Override
  public void save() throws Exception {
    store.update(getRepository().getConfiguration());
    log.debug("Saved");
  }

  @Override
  public <T> T readObject(final AttributesMap attributes, final Class<T> type) {
    checkNotNull(attributes);
    checkNotNull(type);
    log.trace("Reading object type: {}", type);
    return objectMapper.convertValue(attributes.backing(), type);
  }

  @Override
  public <T> T readObject(final String section, final Class<T> type) {
    checkNotNull(section);
    log.trace("Reading section: {}", section);
    AttributesMap attributes = getRepository().getConfiguration().attributes(section);
    return readObject(attributes, type);
  }

  @Override
  public void validate(final Object value, final Class<?>... groups) {
    checkNotNull(value);
    checkNotNull(groups);

    if (log.isTraceEnabled()) {
      log.trace("Validating: {} in groups: {}", value, Arrays.asList(groups));
    }

    Validator validator = validatorFactory.getValidator();
    Set<ConstraintViolation<Object>> violations = validator.validate(value, groups);

    // render log warning and throw exception if any constraints were violated
    if (!violations.isEmpty()) {
      String message = String.format("Validation failed; %d constraints violated", violations.size());

      if (log.isWarnEnabled()) {
        StringBuilder buff = new StringBuilder();
        int c = 0;
        for (ConstraintViolation<Object> violation : violations) {
          buff.append("  ").append(++c).append(") ")
              .append(violation.getMessage())
              .append(", type: ")
              .append(violation.getRootBeanClass())
              .append(", property: ")
              .append(violation.getPropertyPath())
              .append(", value: ")
              .append(violation.getInvalidValue())
              .append(System.lineSeparator());
        }
        log.warn("{}:{}{}", message, System.lineSeparator(), buff);
      }

      throw new ConstraintViolationException(message, violations);
    }
  }
}
