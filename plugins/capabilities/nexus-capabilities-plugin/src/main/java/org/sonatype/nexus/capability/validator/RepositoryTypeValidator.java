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
package org.sonatype.nexus.capability.validator;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.capability.CapabilityDescriptorRegistry;
import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.capability.ValidationResult;
import org.sonatype.nexus.capability.Validator;
import org.sonatype.nexus.capability.support.ValidatorSupport;
import org.sonatype.nexus.capability.support.validator.DefaultValidationResult;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Validator} that ensures that capability repository property references a repository of specified kind(s).
 *
 * @since capabilities 2.0
 */
@Named
public class RepositoryTypeValidator
    extends ValidatorSupport
    implements Validator
{

  private final RepositoryManager repositoryManager;

  private final String propertyKey;

  private final String repositoryType;

  @Inject
  RepositoryTypeValidator(final RepositoryManager repositoryManager,
                          final Provider<CapabilityDescriptorRegistry> capabilityDescriptorRegistryProvider,
                          final @Assisted CapabilityType type,
                          final @Assisted String propertyKey,
                          final @Assisted String repositoryType)
  {
    super(capabilityDescriptorRegistryProvider, type);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.propertyKey = checkNotNull(propertyKey);
    this.repositoryType = checkNotNull(repositoryType);
  }

  @Override
  public ValidationResult validate(final Map<String, String> properties) {
    String repositoryName = properties.get(propertyKey);
    if (repositoryName != null) {
      Repository repository = repositoryManager.get(repositoryName);
      if (repository == null) {
        return new DefaultValidationResult().add(propertyKey, buildMessage(repositoryName));
      }
      if (!repositoryType.equals(repository.getType().getValue())) {
        return new DefaultValidationResult().add(propertyKey, buildMessage(repository));
      }
    }
    return ValidationResult.VALID;
  }

  @Override
  public String explainValid() {
    final StringBuilder message = new StringBuilder();
    message.append(propertyName(propertyKey)).append(" is a ").append(repositoryType).append(" repository");
    return message.toString();
  }

  @Override
  public String explainInvalid() {
    final StringBuilder message = new StringBuilder();
    message.append(propertyName(propertyKey)).append(" is not a ").append(repositoryType)
        .append(" repository");
    return message.toString();

  }

  private String buildMessage(final Repository repository) {
    final StringBuilder message = new StringBuilder();
    message.append("Selected ").append(propertyName(propertyKey).toLowerCase())
        .append(" '").append(repository.getName())
        .append("' must be a ").append(repositoryType).append(" repository");
    return message.toString();
  }

  private String buildMessage(final String repositoryName) {
    final StringBuilder message = new StringBuilder();
    message.append("Selected ").append(propertyName(propertyKey).toLowerCase())
        .append(" '").append(repositoryName).append("' could not be found");
    return message.toString();
  }

}
