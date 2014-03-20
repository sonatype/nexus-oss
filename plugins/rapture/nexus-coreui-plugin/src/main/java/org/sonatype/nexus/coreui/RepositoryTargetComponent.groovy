/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.commons.lang.StringUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry
import org.sonatype.nexus.proxy.targets.Target
import org.sonatype.nexus.proxy.targets.TargetRegistry

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Repository Target {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RepositoryTarget')
class RepositoryTargetComponent
extends DirectComponentSupport
{
  @Inject
  TargetRegistry targetRegistry

  @Inject
  RepositoryTypeRegistry repositoryTypeRegistry

  @Inject
  NexusConfiguration nexusConfiguration

  @DirectMethod
  @RequiresPermissions('nexus:targets:read')
  List<RepositoryTargetXO> read() {
    return targetRegistry.repositoryTargets.collect { input ->
      asRepositoryTarget(input)
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:targets:create')
  RepositoryTargetXO create(final RepositoryTargetXO target) {
    validate(target)
    target.id = Long.toHexString(System.nanoTime())
    def result = new Target(
        target.id, target.name, repositoryTypeRegistry.contentClasses[target.format], target.patterns
    )
    targetRegistry.addRepositoryTarget(result)
    nexusConfiguration.saveConfiguration()
    return asRepositoryTarget(result)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:targets:update')
  RepositoryTargetXO update(final RepositoryTargetXO target) {
    validate(target)
    // TODO validate id and that id exists
    if (target.id) {
      def result = new Target(
          target.id, target.name, repositoryTypeRegistry.contentClasses[target.format], target.patterns
      )
      targetRegistry.addRepositoryTarget(result)
      nexusConfiguration.saveConfiguration()
      return asRepositoryTarget(result)
    }
    // TODO throw exception
    return null
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:targets:delete')
  void delete(final String id) {
    targetRegistry.removeRepositoryTarget(id)
    nexusConfiguration.saveConfiguration()
  }

  private static RepositoryTargetXO asRepositoryTarget(Target input) {
    return new RepositoryTargetXO(
        id: input.id,
        name: input.name,
        format: input.contentClass.id,
        patterns: input.patternTexts.toList().sort()
    )
  }

  private void validate(final RepositoryTargetXO target) {
    def validations = new ValidationResponse()

    if (StringUtils.isBlank(target.name)) {
      validations.addValidationError(new ValidationMessage('name', 'Name cannot be empty'))
    }
    if (StringUtils.isBlank(target.format)) {
      validations.addValidationError(new ValidationMessage('repositoryFormat', 'Repository type cannot be empty'))
    }
    else {
      def contentClass = repositoryTypeRegistry.contentClasses[target.format]
      if (!contentClass) {
        validations.addValidationError(new ValidationMessage('repositoryFormat', 'Repository type does not exist'))
      }
    }
    if (!target.patterns || target.patterns.empty) {
      validations.addValidationError(new ValidationMessage('patterns', 'The target should have at least one pattern'))
    }

    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
  }

}
