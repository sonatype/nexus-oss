/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Repository Route {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RepositoryRoute')
class RepositoryRouteComponent
extends DirectComponentSupport
{

  @Inject
  RequestRepositoryMapper repositoryMapper

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  RepositoryRegistry repositoryRegistry

  /**
   * Retrieve repository routes.
   * @return a list of repository routes
   */
  @DirectMethod
  @RequiresPermissions('nexus:routes:read')
  List<RepositoryRouteXO> read() {
    return repositoryMapper.mappings.values().findResults { RepositoryPathMapping input ->
      try {
        // access group/mapped repositories to ensure user has access to them
        if (input.groupId != '*') {
          repositoryRegistry.getRepository(input.groupId)
        }
        input.mappedRepositories?.each { repositoryId ->
          repositoryRegistry.getRepository(repositoryId)
        }
        return input
      }
      catch (NoSuchRepositoryAccessException ignore) {
        return null
      }
    }.collect { input ->
      asRepositoryRoute(input)
    }
  }

  /**
   * Creates a repository route.
   * @param routeXO to be created
   * @return created repository route
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryRouteXO create(final @NotNull(message = '[route] may not be null') @Valid RepositoryRouteXO routeXO) {
    routeXO.id = Long.toHexString(System.nanoTime())
    return addToMapper(routeXO)
  }

  /**
   * Updates a repository route.
   * @param routeXO to be updated
   * @return updated repository route
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryRouteXO update(final @NotNull(message = '[route] may not be null') @Valid RepositoryRouteXO routeXO) {
    if (repositoryMapper.mappings[routeXO.id]) {
      return addToMapper(routeXO)
    }
    throw new IllegalArgumentException('Route with id "' + routeXO.id + '" not found')
  }

  /**
   * Deletes a repository route.
   * @param id of repository route to be deleted
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:delete')
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id) {
    if (repositoryMapper.removeMapping(id)) {
      nexusConfiguration.saveConfiguration()
    }
    else {
      throw new IllegalArgumentException('Route with id "' + id + '" not found')
    }
  }

  RepositoryRouteXO asRepositoryRoute(final RepositoryPathMapping input) {
    return new RepositoryRouteXO(
        id: input.id,
        pattern: input.patterns[0],
        mappingType: input.mappingType,
        groupId: input.groupId,
        groupName: input.groupId == '*' ? '*' : repositoryRegistry.getRepository(input.groupId).name,
        mappedRepositoriesIds: input.mappedRepositories,
        mappedRepositoriesNames: input.mappedRepositories.collect { repositoryId ->
          return repositoryRegistry.getRepository(repositoryId).name
        }
    )
  }

  RepositoryRouteXO addToMapper(final RepositoryRouteXO routeXO) {
    validate(routeXO)
    def route = new RepositoryPathMapping(
        routeXO.id, routeXO.mappingType, routeXO.groupId, [routeXO.pattern], routeXO.mappedRepositoriesIds
    )
    repositoryMapper.addMapping(route)
    nexusConfiguration.saveConfiguration()
    return asRepositoryRoute(route)
  }

  private static void validate(final RepositoryRouteXO routeXO) {
    def validations = new ValidationResponse()

    try {
      Pattern.compile(routeXO.pattern)
    }
    catch (PatternSyntaxException e) {
      validations.addValidationError(new ValidationMessage('pattern', 'Not a valid regular expression'))
    }

    if (routeXO.mappingType == RepositoryPathMapping.MappingType.BLOCKING) {
      if (routeXO.mappedRepositoriesIds && !routeXO.mappedRepositoriesIds.empty) {
        validations.addValidationError(new ValidationMessage(
            '*', 'Blocking rule does not allow selecting repositories')
        )
      }
    }
    else {
      if (!routeXO.mappedRepositoriesIds || routeXO.mappedRepositoriesIds.empty) {
        validations.addValidationError(new ValidationMessage(
            'mappedRepositories', 'At least one repository must be selected')
        )
      }
    }

    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
  }

}
