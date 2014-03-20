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
import org.sonatype.nexus.proxy.NoSuchRepositoryException
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

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
   * Retrieve a list of available repository routes.
   */
  @DirectMethod
  @RequiresPermissions('nexus:routes:read')
  List<RepositoryRouteXO> read() {
    return repositoryMapper.mappings.values().findResults { input ->
      try {
        return asRepositoryRoute(input)
      }
      catch (NoSuchRepositoryException e) {
        return null
      }
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:create')
  RepositoryRouteXO create(final RepositoryRouteXO routeXO) {
    routeXO.id = Long.toHexString(System.nanoTime())
    return addToMapper(routeXO)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:update')
  RepositoryRouteXO update(final RepositoryRouteXO routeXO) {
    if (routeXO.id) {
      if (repositoryMapper.mappings[routeXO.id]) {
        return addToMapper(routeXO)
      }
      throw new IllegalArgumentException('Route with id "' + routeXO.id + '" not found')
    }
    throw new IllegalArgumentException('Missing id for route to be updated')
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:routes:delete')
  void delete(final String id) {
    if (repositoryMapper.removeMapping(id)) {
      nexusConfiguration.saveConfiguration()
    }
    else {
      throw new IllegalArgumentException('Route with id "' + id + '" not found')
    }
  }

  def asRepositoryRoute(final RepositoryPathMapping input) {
    return new RepositoryRouteXO(
        id: input.id,
        pattern: input.patterns[0],
        mappingType: input.mappingType,
        groupId: input.groupId,
        groupName: input.groupId != '*' ? repositoryName(input.id, input.groupId) : input.groupId,
        mappedRepositoriesIds: input.mappedRepositories,
        mappedRepositoriesNames: input.mappedRepositories.collect { repositoryId ->
          return repositoryName(input.id, repositoryId)
        }
    )
  }

  def repositoryName(final String routeId, final String repositoryId) {
    try {
      return repositoryRegistry.getRepository(repositoryId).name
    }
    catch (NoSuchRepositoryAccessException e) {
      log.debug("Access Denied to group '{}' contained within route: '{}", repositoryId, routeId)
      throw e
    }
    catch (NoSuchRepositoryException e) {
      log.debug("Cannot find repository '{}' contained within route: '{}", repositoryId, routeId)
      throw e
    }
  }

  def addToMapper(final RepositoryRouteXO routeXO) {
    validate(routeXO)
    def route = new RepositoryPathMapping(
        routeXO.id, routeXO.mappingType, routeXO.groupId, [routeXO.pattern], routeXO.mappedRepositoriesIds
    )
    repositoryMapper.addMapping(route)
    nexusConfiguration.saveConfiguration()
    return asRepositoryRoute(route)
  }

  static validate(final RepositoryRouteXO routeXO) {
    def validations = new ValidationResponse()

    if (StringUtils.isBlank(routeXO.pattern)) {
      validations.addValidationError(new ValidationMessage('pattern', 'Pattern cannot be empty'))
    }
    if (StringUtils.isBlank(routeXO.groupId)) {
      validations.addValidationError(new ValidationMessage('groupId', 'Group cannot be empty'))
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
