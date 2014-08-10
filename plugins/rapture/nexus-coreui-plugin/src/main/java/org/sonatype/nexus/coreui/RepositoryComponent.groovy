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

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import com.softwarementors.extjs.djn.config.annotations.DirectPollMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.codehaus.plexus.util.StringUtils
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.configuration.model.CLocalStorage
import org.sonatype.nexus.configuration.model.CRemoteAuthentication
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings
import org.sonatype.nexus.configuration.model.CRemoteStorage
import org.sonatype.nexus.configuration.model.CRepository
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.proxy.RemoteStorageException
import org.sonatype.nexus.proxy.ResourceStoreRequest
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.maven.MavenHostedRepository
import org.sonatype.nexus.proxy.maven.MavenProxyRepository
import org.sonatype.nexus.proxy.maven.MavenRepository
import org.sonatype.nexus.proxy.maven.RepositoryPolicy
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry
import org.sonatype.nexus.proxy.repository.AbstractRepository
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings
import org.sonatype.nexus.proxy.repository.GroupRepository
import org.sonatype.nexus.proxy.repository.HostedRepository
import org.sonatype.nexus.proxy.repository.LocalStatus
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings
import org.sonatype.nexus.proxy.repository.ProxyMode
import org.sonatype.nexus.proxy.repository.ProxyRepository
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings
import org.sonatype.nexus.proxy.repository.RemoteStatus
import org.sonatype.nexus.proxy.repository.Repository
import org.sonatype.nexus.proxy.repository.ShadowRepository
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings
import org.sonatype.nexus.proxy.storage.remote.RemoteProviderHintFactory
import org.sonatype.nexus.rapture.TrustStoreKeys
import org.sonatype.nexus.scheduling.NexusScheduler
import org.sonatype.nexus.tasks.ExpireCacheTask
import org.sonatype.nexus.templates.TemplateManager
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider
import org.sonatype.nexus.templates.repository.RepositoryTemplate
import org.sonatype.nexus.templates.repository.maven.AbstractMavenRepositoryTemplate
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.groups.Default
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Repository {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Repository')
class RepositoryComponent
extends DirectComponentSupport
{

  private static final TRUST_STORE_TYPE = 'repository'

  @Inject
  @Named("protected")
  RepositoryRegistry protectedRepositoryRegistry

  @Inject
  RepositoryTypeRegistry repositoryTypeRegistry

  @Inject
  UrlBuilder urlBuilder

  @Inject
  TemplateManager templateManager

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  DefaultRepositoryTemplateProvider repositoryTemplateProvider

  @Inject
  RemoteProviderHintFactory remoteProviderHintFactory

  @Inject
  @Named('nexus-uber')
  ClassLoader uberClassLoader

  @Inject
  NexusScheduler nexusScheduler

  @Inject
  @Nullable
  TrustStoreKeys trustStoreKeys

  @Inject
  Validator validator

  def typesToClass = [
      'proxy': ProxyRepository.class,
      'hosted': HostedRepository.class,
      'shadow': ShadowRepository.class,
      'group': GroupRepository.class
  ]

  /**
   * Retrieve a list of available repositories.
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<RepositoryXO> read(final @Nullable StoreLoadParameters parameters) {
    def templates = readTemplates(null)
    return filter(parameters).collect { repository ->
      asRepositoryXO(repository, templates)
    }
  }

  /**
   * Retrieve repositories status.
   */
  @DirectPollMethod(event = "coreui_Repository_readStatus")
  @RequiresPermissions('nexus:repostatus:read')
  List<RepositoryStatusXO> readStatus(final Map<String, String> params) {
    return readStatus(Boolean.valueOf(params['forceCheck']))
  }

  @DirectMethod
  @RequiresPermissions('nexus:repostatus:read')
  List<RepositoryStatusXO> readStatus(final boolean forceCheck) {
    return protectedRepositoryRegistry.repositories.collect { repository ->
      def repositoryStatus = new RepositoryStatusXO(
          id: repository.id,
          localStatus: repository.localStatus
      )
      repository.adaptToFacet(ProxyRepository)?.with { proxyRepository ->
        repositoryStatus.proxyMode = proxyRepository.proxyMode
        proxyRepository.getRemoteStatus(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT), forceCheck)?.with { RemoteStatus status ->
          repositoryStatus.remoteStatus = status
          repositoryStatus.remoteStatusReason = status.reason
        }
      }
      return repositoryStatus
    }
  }

  /**
   * Retrieve a list of available repositories references.
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<RepositoryReferenceXO> readReferences(final @Nullable StoreLoadParameters parameters) {
    return filter(parameters).collect { repository ->
      new RepositoryReferenceXO(
          id: repository.id,
          name: repository.name,
          type: typeOf(repository),
          format: repository.repositoryContentClass.id
      )
    }
  }

  /**
   * Retrieve a list of available repositories references and adds an entry for all repositories with id '*'.
   */
  @DirectMethod
  List<RepositoryReferenceXO> readReferencesAddingEntryForAll(final @Nullable StoreLoadParameters parameters) {
    List<RepositoryReferenceXO> references = readReferences(parameters)
    references << new RepositoryReferenceXO(
        id: '*',
        name: '(All Repositories)'
    )
  }

  /**
   * Retrieve a list of available repository templates.
   */
  @DirectMethod
  @RequiresPermissions('nexus:componentsrepotypes:read')
  List<RepositoryTemplateXO> readTemplates(final @Nullable StoreLoadParameters parameters) {
    def templates = []
    def typeFilter = parameters?.getFilter('type')
    def formatFilter = parameters?.getFilter('format')
    def policyFilter = parameters?.getFilter('policy')
    RepositoryPolicy policy = null
    if (policyFilter && !policyFilter.trim().isEmpty()) {
      policy = RepositoryPolicy.valueOf(policyFilter)
    }
    def types = typesToClass
    templateManager.templates.getTemplates(RepositoryTemplate.class).templatesList.each { RepositoryTemplate template ->
      types.each {
        if (template.targetFits(it.value)) {
          def masterFormat = null,
              type = it.key == 'shadow' ? 'virtual' : it.key,
              format = template.contentClass.id

          if (type == 'virtual' && template.contentClass.id.startsWith('maven')) {
            masterFormat = template.contentClass.id == 'maven1' ? 'maven2' : 'maven1'
          }
          if (type == (typeFilter ?: type)
              && format == (formatFilter ?: format)
              && (!policy || !(template instanceof AbstractMavenRepositoryTemplate) || policy == template.repositoryPolicy)) {
            templates.add(
                new RepositoryTemplateXO(
                    id: template.id,
                    type: type,
                    provider: template.repositoryProviderHint,
                    providerName: template.description,
                    format: template.contentClass.id,
                    formatName: template.contentClass.name,
                    masterFormat: masterFormat
                )
            )
          }
        }
      }
    }
    return templates
  }

  /**
   * Retrieve a list of available content classes.
   */
  @DirectMethod
  @RequiresPermissions('nexus:componentscontentclasses:read')
  List<RepositoryFormatXO> readFormats() {
    repositoryTypeRegistry.contentClasses.collect {
      new RepositoryFormatXO(
          id: it.value.id,
          name: it.value.name
      )
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createGroup(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryGroupXO repositoryXO) {
    create(repositoryXO, doCreate, doCreateGroup)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateGroup(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryGroupXO repositoryXO) {
    update(repositoryXO, GroupRepository.class, doUpdate, doUpdateGroup)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createHosted(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryHostedXO repositoryXO) {
    create(repositoryXO, doCreate, doCreateHosted)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateHosted(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryHostedXO repositoryXO) {
    update(repositoryXO, HostedRepository.class, doUpdate, doUpdateHosted)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createHostedMaven(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryHostedMavenXO repositoryXO) {
    create(repositoryXO, doCreate, doCreateHosted, doCreateHostedMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateHostedMaven(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryHostedMavenXO repositoryXO) {
    update(repositoryXO, MavenHostedRepository.class, doUpdate, doUpdateHosted, doUpdateHostedMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createProxy(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryProxyXO repositoryXO) {
    RepositoryXO created = create(repositoryXO, doCreate, doCreateProxy)
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, repositoryXO.id, repositoryXO.useTrustStoreForRemoteStorageUrl)
    return created
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateProxy(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryProxyXO repositoryXO) {
    RepositoryXO updated = update(repositoryXO, ProxyRepository.class, doUpdate, doUpdateProxy)
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, repositoryXO.id, repositoryXO.useTrustStoreForRemoteStorageUrl)
    return updated
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createProxyMaven(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryProxyMavenXO repositoryXO) {
    RepositoryXO created = create(repositoryXO, doCreate, doCreateProxy, doCreateProxyMaven)
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, repositoryXO.id, repositoryXO.useTrustStoreForRemoteStorageUrl)
    return created
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateProxyMaven(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryProxyMavenXO repositoryXO) {
    RepositoryXO updated = update(repositoryXO, MavenProxyRepository.class, doUpdate, doUpdateProxy, doUpdateProxyMaven)
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, repositoryXO.id, repositoryXO.useTrustStoreForRemoteStorageUrl)
    return updated
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  @Validate(groups = [Create.class, Default.class])
  RepositoryXO createVirtual(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryVirtualXO repositoryXO) {
    create(repositoryXO, doCreate, doCreateVirtual)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate(groups = [Update.class, Default.class])
  RepositoryXO updateVirtual(final @NotNull(message = '[repositoryXO] may not be null') @Valid RepositoryVirtualXO repositoryXO) {
    update(repositoryXO, ShadowRepository.class, doUpdate, doUpdateVirtual)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:delete')
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id) {
    protectedRepositoryRegistry.removeRepository(id)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:cache:delete')
  @Validate
  void clearCache(final @NotEmpty(message = '[id] may not be empty') String id,
                  final String path)
  {
    // validate repository id
    protectedRepositoryRegistry.getRepository(id)
    ExpireCacheTask task = nexusScheduler.createTaskInstance(ExpireCacheTask)
    task.setRepositoryId(id)
    task.setResourceStorePath(path)
    nexusScheduler.submit("Clear cache ${id}:${path}", task)
  }

  /**
   * Update local status/proxy mode of a repository. It also updates local status of any dependant shadow repository.
   * @param id of repository
   * @param localStatus new local status (can be null)
   * @param proxyMode new proxy mode (can be null)
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repostatus:update')
  @Validate
  void updateStatus(final @NotEmpty(message = '[id] may not be empty') String id,
                    final @Nullable LocalStatus localStatus,
                    final @Nullable ProxyMode proxyMode)
  {
    Repository repository = protectedRepositoryRegistry.getRepository(id)
    if (localStatus || proxyMode) {
      if (localStatus) {
        repository.localStatus = localStatus
        protectedRepositoryRegistry.getRepositoriesWithFacet(ShadowRepository).each { shadowRepository ->
          if (repository.id == shadowRepository.masterRepository.id) {
            shadowRepository.localStatus = localStatus
          }
        }
      }
      if (proxyMode) {
        if (repository.repositoryKind.isFacetAvailable(ProxyRepository)) {
          repository.adaptToFacet(ProxyRepository).proxyMode = proxyMode
        }
      }
      nexusConfiguration.saveConfiguration()
    }
  }

  def RepositoryXO create(RepositoryXO repositoryXO, Closure... createClosures) {
    def template = templateManager.templates.getTemplateById(repositoryXO.template) as RepositoryTemplate
    CRepository configuration = template.configurableRepository.getCurrentConfiguration(true)
    createClosures.each { createClosure ->
      createClosure(configuration, repositoryXO)
    }
    Repository created = template.create()
    return asRepositoryXO(created, readTemplates(null))
  }

  def <T extends Repository> RepositoryXO update(RepositoryXO repositoryXO, Class<T> repoType, Closure... updateClosures) {
    if (repositoryXO.id) {
      T updated = protectedRepositoryRegistry.getRepositoryWithFacet(repositoryXO.id, repoType)
      updateClosures.each { updateClosure ->
        updateClosure(updated, repositoryXO)
      }
      nexusConfiguration.saveConfiguration()
      return asRepositoryXO(updated, readTemplates(null))
    }
    throw new IllegalArgumentException('Missing id for repository to be updated')
  }

  def static doCreate = { CRepository repo, RepositoryXO repositoryXO ->
    repo.with {
      id = repositoryXO.id
      name = repositoryXO.name
      browseable = repositoryXO.browseable
      exposed = repositoryXO.exposed
      localStatus = LocalStatus.IN_SERVICE
    }
    if (repositoryXO.overrideLocalStorageUrl && !repositoryXO.overrideLocalStorageUrl.trim().empty) {
      if (!repo.localStorage) {
        repo.localStorage = new CLocalStorage()
      }
      repo.localStorage.url = repositoryXO.overrideLocalStorageUrl
      repo.localStorage.provider = 'file'
    }
    else {
      repo.localStorage = null
    }
  }

  def static doUpdate = { Repository repo, RepositoryXO repositoryXO ->
    repo.with {
      name = repositoryXO.name
      browseable = repositoryXO.browseable
      exposed = repositoryXO.exposed
    }
    if (repositoryXO.overrideLocalStorageUrl && !repositoryXO.overrideLocalStorageUrl.trim().empty) {
      repo.localUrl = repositoryXO.overrideLocalStorageUrl
    }
    else {
      repo.localUrl = null
    }
  }

  def static doCreateGroup = { CRepository repo, RepositoryGroupXO repositoryXO ->
    M2GroupRepositoryConfiguration exConf = new M2GroupRepositoryConfiguration(
        repo.externalConfiguration as Xpp3Dom
    )
    exConf.memberRepositoryIds = repositoryXO.memberRepositoryIds
  }

  def static doUpdateGroup = { GroupRepository repo, RepositoryGroupXO repositoryXO ->
    repo.memberRepositoryIds = repositoryXO.memberRepositoryIds
  }

  def static doCreateHosted = { CRepository repo, RepositoryHostedXO repositoryXO ->
    repo.writePolicy = repositoryXO.writePolicy
  }

  def static doUpdateHosted = { HostedRepository repo, RepositoryHostedXO repositoryXO ->
    repo.writePolicy = repositoryXO.writePolicy
  }

  def static doCreateHostedMaven = { CRepository repo, RepositoryHostedMavenXO repositoryXO ->
    repo.indexable = repositoryXO.indexable
  }

  def static doUpdateHostedMaven = { MavenHostedRepository repo, RepositoryHostedMavenXO repositoryXO ->
    repo.indexable = repositoryXO.indexable
  }

  def doCreateProxy = { CRepository repo, RepositoryProxyXO repositoryXO ->
    M2RepositoryConfiguration exConf = new M2RepositoryConfiguration(
        repo.externalConfiguration as Xpp3Dom
    )
    if (repositoryXO.authEnabled) {
      validator.validate(repositoryXO, RepositoryProxyXO.Authentication)
    }
    if (repositoryXO.httpRequestSettings) {
      validator.validate(repositoryXO, RepositoryProxyXO.HttpRequestSettings)
    }
    if (!repo.remoteStorage) {
      repo.remoteStorage = new CRemoteStorage()
    }
    repo.remoteStorage.url = repositoryXO.remoteStorageUrl
    repo.remoteStorage.provider = remoteProviderHintFactory.getDefaultRoleHint(repositoryXO.remoteStorageUrl)
    exConf.autoBlockActive = repositoryXO.autoBlockActive
    exConf.fileTypeValidation = repositoryXO.fileTypeValidation
    if (repositoryXO.notFoundCacheTTL != null) repo.notFoundCacheTTL = repositoryXO.notFoundCacheTTL
    if (repositoryXO.itemMaxAge != null) exConf.itemMaxAge = repositoryXO.itemMaxAge
    if (repositoryXO.authEnabled) {
      repo.remoteStorage.authentication = new CRemoteAuthentication(
          username: repositoryXO.authUsername,
          password: repositoryXO.authPassword?.valueIfValid,
          ntlmDomain: repositoryXO.authNtlmDomain,
          ntlmHost: repositoryXO.authNtlmHost
      )
    }
    if (repositoryXO.httpRequestSettings) {
      repo.remoteStorage.connectionSettings = new CRemoteConnectionSettings(
          userAgentCustomizationString: repositoryXO.userAgentCustomisation,
          queryString: repositoryXO.urlParameters,
          connectionTimeout: repositoryXO.timeout ?: 20 * 1000,
          retrievalRetryCount: repositoryXO.retries ?: 3
      )
    }
  }

  def doUpdateProxy = { ProxyRepository repo, RepositoryProxyXO repositoryXO ->
    if (repositoryXO.authEnabled) {
      validator.validate(repositoryXO, RepositoryProxyXO.Authentication)
    }
    if (repositoryXO.httpRequestSettings) {
      validator.validate(repositoryXO, RepositoryProxyXO.HttpRequestSettings)
    }

    try {
      repo.remoteUrl = repositoryXO.remoteStorageUrl
    }
    catch (RemoteStorageException e) {
      def validations = new ValidationResponse()
      validations.addValidationError(new ValidationMessage("remoteStorageUrl", e.getMessage()))
      throw new InvalidConfigurationException(validations);
    }
    repo.autoBlockActive = repositoryXO.autoBlockActive
    repo.fileTypeValidation = repositoryXO.fileTypeValidation
    if (repositoryXO.notFoundCacheTTL != null) repo.notFoundCacheTimeToLive = repositoryXO.notFoundCacheTTL
    if (repositoryXO.itemMaxAge != null) repo.itemMaxAge = repositoryXO.itemMaxAge
    if (repositoryXO.authEnabled) {
      if (repositoryXO.authNtlmHost || repositoryXO.authNtlmDomain) {
        repo.remoteAuthenticationSettings = new NtlmRemoteAuthenticationSettings(
            repositoryXO.authUsername,
            getPassword(repositoryXO.authPassword, repo.remoteAuthenticationSettings),
            repositoryXO.authNtlmDomain, repositoryXO.authNtlmHost
        )
      }
      else {
        repo.remoteAuthenticationSettings = new UsernamePasswordRemoteAuthenticationSettings(
            repositoryXO.authUsername,
            getPassword(repositoryXO.authPassword, repo.remoteAuthenticationSettings),
        )
      }
    }
    else {
      repo.remoteStorageContext?.removeRemoteAuthenticationSettings()
    }

    if (repositoryXO.httpRequestSettings) {
      repo.remoteConnectionSettings = new DefaultRemoteConnectionSettings(
          userAgentCustomizationString: repositoryXO.userAgentCustomisation,
          queryString: repositoryXO.urlParameters,
          connectionTimeout: repositoryXO.timeout ?: 20 * 1000,
          retrievalRetryCount: repositoryXO.retries ?: 3
      )
    }
    else {
      repo.remoteStorageContext?.removeRemoteConnectionSettings()
    }
  }

  def static doCreateProxyMaven = { CRepository repo, RepositoryProxyMavenXO repositoryXO ->
    M2RepositoryConfiguration exConf = new M2RepositoryConfiguration(
        repo.externalConfiguration as Xpp3Dom
    )
    exConf.downloadRemoteIndex = repositoryXO.downloadRemoteIndexes
    if (repositoryXO.checksumPolicy != null) exConf.checksumPolicy = repositoryXO.checksumPolicy
    if (repositoryXO.artifactMaxAge != null) exConf.artifactMaxAge = repositoryXO.artifactMaxAge
    if (repositoryXO.metadataMaxAge != null) exConf.metadataMaxAge = repositoryXO.metadataMaxAge
  }

  def static doUpdateProxyMaven = { MavenProxyRepository repo, RepositoryProxyMavenXO repositoryXO ->
    repo.downloadRemoteIndexes = repositoryXO.downloadRemoteIndexes
    if (repositoryXO.checksumPolicy != null) repo.checksumPolicy = repositoryXO.checksumPolicy
    if (repositoryXO.artifactMaxAge != null) repo.artifactMaxAge = repositoryXO.artifactMaxAge
    if (repositoryXO.metadataMaxAge != null) repo.metadataMaxAge = repositoryXO.metadataMaxAge
  }

  def static doCreateVirtual = { CRepository repo, RepositoryVirtualXO repositoryXO ->
    // HACK: we should not use a Maven2->Maven1 config
    M2LayoutedM1ShadowRepositoryConfiguration exConf = new M2LayoutedM1ShadowRepositoryConfiguration(
        repo.externalConfiguration as Xpp3Dom
    )
    exConf.masterRepositoryId = repositoryXO.shadowOf
    exConf.synchronizeAtStartup = repositoryXO.synchronizeAtStartup
  }

  def doUpdateVirtual = { ShadowRepository repo, RepositoryVirtualXO repositoryXO ->
    repo.synchronizeAtStartup = repositoryXO.synchronizeAtStartup
    repo.masterRepository = protectedRepositoryRegistry.getRepository(repositoryXO.shadowOf)
  }

  def asRepositoryXO(Repository repo, List<RepositoryTemplateXO> templates) {
    def RepositoryXO xo = null
    repo.adaptToFacet(MavenHostedRepository.class)?.with {
      xo = doGetMavenHosted(it, xo)
    }
    repo.adaptToFacet(HostedRepository.class)?.with {
      xo = doGetHosted(it, xo)
    }
    repo.adaptToFacet(MavenProxyRepository.class)?.with {
      xo = doGetMavenProxy(it, xo)
    }
    repo.adaptToFacet(ProxyRepository.class)?.with {
      xo = doGetProxy(it, xo)
    }
    repo.adaptToFacet(GroupRepository.class)?.with {
      xo = doGetGroup(it, xo)
    }
    repo.adaptToFacet(ShadowRepository.class)?.with {
      xo = doGetVirtual(it, xo)
    }
    return doGet(repo, xo, templates)
  }

  def static doGetMavenHosted(MavenHostedRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryHostedMavenXO()
    if (xo instanceof RepositoryHostedMavenXO) {
      xo.with {
        indexable = repo.indexable
        repositoryPolicy = repo.repositoryPolicy
      }
    }
    return xo
  }

  def static doGetHosted(HostedRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryHostedXO()
    if (xo instanceof RepositoryHostedXO) {
      xo.with {
        writePolicy = repo.writePolicy
      }
    }
    return xo
  }

  def static doGetMavenProxy(MavenProxyRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryProxyMavenXO()
    if (xo instanceof RepositoryProxyMavenXO) {
      xo.with {
        repositoryPolicy = repo.repositoryPolicy
        downloadRemoteIndexes = repo.downloadRemoteIndexes
        checksumPolicy = repo.checksumPolicy
        artifactMaxAge = repo.artifactMaxAge
        metadataMaxAge = repo.metadataMaxAge
      }
    }
    return xo
  }

  def doGetProxy(ProxyRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryProxyXO()
    if (xo instanceof RepositoryProxyXO) {
      xo.with {
        def rsc = repo.remoteStorageContext
        def rcs = rsc?.remoteConnectionSettings
        proxyMode = repo.proxyMode
        remoteStorageUrl = repo.remoteUrl
        useTrustStoreForRemoteStorageUrl = trustStoreKeys?.isEnabled(TRUST_STORE_TYPE, repo.id)
        autoBlockActive = repo.autoBlockActive
        fileTypeValidation = repo.fileTypeValidation
        userAgentCustomisation = rcs?.userAgentCustomizationString
        urlParameters = rcs?.queryString
        timeout = rcs?.connectionTimeout ? rcs?.connectionTimeout == 0 ? 0 : rcs?.connectionTimeout / 1000 : null
        retries = rcs?.retrievalRetryCount
        httpRequestSettings = rsc?.hasRemoteConnectionSettings() && (userAgentCustomisation || urlParameters || timeout || retries)
        notFoundCacheTTL = repo.notFoundCacheTimeToLive
        itemMaxAge = repo.itemMaxAge
        authEnabled = false
        rsc?.remoteAuthenticationSettings?.with { ras ->
          authEnabled = true
          if (ras instanceof UsernamePasswordRemoteAuthenticationSettings) {
            authUsername = ras.username
            authPassword = Password.fakePassword()
          }
          if (ras instanceof NtlmRemoteAuthenticationSettings) {
            authNtlmHost = ras.ntlmHost
            authNtlmDomain = ras.ntlmDomain
          }
          return
        }
        repo.getRemoteStatus(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT), false)?.with { RemoteStatus status ->
          remoteStatus = status
          remoteStatusReason = status.reason
        }
      }
    }
    return xo
  }

  def static doGetGroup(GroupRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryGroupXO()
    if (xo instanceof RepositoryGroupXO) {
      xo.with {
        memberRepositoryIds = repo.memberRepositoryIds
      }
    }
    return xo
  }

  def static doGetVirtual(ShadowRepository repo, RepositoryXO xo) {
    if (!xo) xo = new RepositoryVirtualXO()
    if (xo instanceof RepositoryVirtualXO) {
      xo.with {
        synchronizeAtStartup = repo.synchronizeAtStartup
        shadowOf = repo.masterRepository.id
      }
    }
    return xo
  }

  def doGet(Repository repo, RepositoryXO xo, List<RepositoryTemplateXO> templates) {
    if (!xo) xo = new RepositoryXO()
    if (xo instanceof RepositoryXO) {
      xo.with {
        id = repo.id
        name = repo.name
        type = typeOf(repo)
        provider = repo.providerHint
        providerName = nameOfProvider(templates, xo.type, xo.provider)
        format = repo.repositoryContentClass.id
        formatName = repo.repositoryContentClass.name
        browseable = repo.browseable
        exposed = repo.exposed
        localStatus = repo.localStatus
        url = urlBuilder.getExposedRepositoryContentUrl(repo)
        userManaged = repo.userManaged
      }
      if (repo instanceof AbstractRepository) {
        xo.defaultLocalStorageUrl = repo.currentCoreConfiguration.getConfiguration(false).defaultLocalStorageUrl
        xo.overrideLocalStorageUrl = repo.currentCoreConfiguration.getConfiguration(false).localStorage.url
        if (StringUtils.isBlank(xo.overrideLocalStorageUrl)) {
          xo.overrideLocalStorageUrl = null
        }
      }
    }
    return xo
  }

  def static typeOf(Repository repository) {
    def kind = repository.repositoryKind
    if (kind.isFacetAvailable(ProxyRepository.class)) {
      return 'proxy'
    }
    else if (kind.isFacetAvailable(HostedRepository.class)) {
      return 'hosted'
    }
    else if (kind.isFacetAvailable(ShadowRepository.class)) {
      return 'virtual'
    }
    else if (kind.isFacetAvailable(GroupRepository.class)) {
      return 'group'
    }
    return null
  }

  private static final Pattern BRACKETS_PATTERN = Pattern.compile("(.*)( \\(.*\\))")

  def static String nameOfProvider(List<RepositoryTemplateXO> templates, type, provider) {
    def template = templates.find { template -> template.type == type && template.provider == provider }
    def name = template?.providerName
    if (name) {
      Matcher m = BRACKETS_PATTERN.matcher(name)
      if (m.matches() && m.groupCount() == 2) {
        name = m.group(1)
      }
    }
    return name
  }

  def static String getPassword(Password password, RemoteAuthenticationSettings settings) {
    if (password?.valid) {
      return password.value
    }
    if (settings instanceof UsernamePasswordRemoteAuthenticationSettings) {
      return settings.password
    }
    return null
  }

  List<Repository> filter(final @Nullable StoreLoadParameters parameters) {
    List<Repository> repositories = protectedRepositoryRegistry.repositories
    boolean includeUserManaged = true
    boolean includeNexusManaged = false
    if (parameters) {
      def filter = { predicate ->
        repositories = repositories.findResults { Repository repository ->
          predicate.apply(repository) ? repository : null
        }
      }

      String[] types = parameters.getFilter('type')?.split(',')
      hasAnyOfFacets(types)?.with(filter)
      hasNoneOfFacets(types)?.with(filter)

      String[] contentClasses = parameters.getFilter('format')?.split(',')
      hasAnyOfContentClasses(contentClasses)?.with(filter)
      hasNoneOfContentClasses(contentClasses)?.with(filter)

      def includeUserManagedFilter = parameters.getFilter('includeUserManaged')
      if (includeUserManagedFilter) {
        includeUserManaged = Boolean.valueOf(includeUserManagedFilter)
      }
      def includeNexusManagedFilter = parameters.getFilter('includeNexusManaged')
      if (includeNexusManagedFilter) {
        includeNexusManaged = Boolean.valueOf(includeNexusManagedFilter)
      }
    }
    if (!includeUserManaged) {
      repositories = repositories.findResults { Repository repository ->
        return repository.userManaged ? null : repository
      }
    }
    if (!includeNexusManaged) {
      repositories = repositories.findResults { Repository repository ->
        return repository.userManaged ? repository : null
      }
    }
    String policyFilter = parameters.getFilter('policy')
    if (policyFilter && !policyFilter.trim().isEmpty()) {
      RepositoryPolicy policy = RepositoryPolicy.valueOf(policyFilter)
      repositories = repositories.findResults { Repository repository ->
        if (repository instanceof MavenRepository && repository.repositoryPolicy != policy) {
          return null
        }
        return repository
      }
    }
    return repositories
  }

  Predicate<Repository> hasAnyOfFacets(@Nullable final String[] facets) {
    if (facets) {
      List<Predicate<Repository>> predicates = []
      facets.each { String facet ->
        if (StringUtils.isNotEmpty(facet) && !facet.startsWith('!')) {
          try {
            Class<?> facetClass = typesToClass[facet]
            if (!facetClass) {
              facetClass = uberClassLoader.loadClass(facet)
            }
            predicates << new Predicate<Repository>() {
              @Override
              public boolean apply(@Nullable final Repository input) {
                return input && input.repositoryKind.isFacetAvailable(facetClass)
              }
            }
          }
          catch (ClassNotFoundException e) {
            log.warn('Repositories will not be filtered by facet {} as it could not be loaded', facet)
          }
        }
      }
      if (!predicates.empty) {
        if (predicates.size() == 1) {
          return predicates[0]
        }
        return Predicates.or(predicates)
      }
    }
    return null
  }

  Predicate<Repository> hasNoneOfFacets(@Nullable final String[] facets) {
    if (facets) {
      List<Predicate<Repository>> predicates = []
      facets.each { String facet ->
        if (StringUtils.isNotEmpty(facet) && facet.startsWith('!')) {
          String actualFacet = facet.substring(1)
          try {
            Class<?> facetClass = typesToClass[actualFacet]
            if (!facetClass) {
              facetClass = uberClassLoader.loadClass(actualFacet)
            }
            predicates << new Predicate<Repository>() {
              @Override
              public boolean apply(@Nullable final Repository input) {
                return input && !input.repositoryKind.isFacetAvailable(facetClass)
              }
            }
          }
          catch (ClassNotFoundException e) {
            log.warn('Repositories will not be filtered by facet {} as it could not be loaded', actualFacet)
          }
        }
      }
      if (!predicates.empty) {
        if (predicates.size() == 1) {
          return predicates[0]
        }
        return Predicates.and(predicates)
      }
    }
    return null
  }

  private static Predicate<Repository> hasAnyOfContentClasses(final String[] contentClasses) {
    if (contentClasses) {
      List<Predicate<Repository>> predicates = []
      contentClasses.each { String contentClass ->
        if (StringUtils.isNotEmpty(contentClass) && !contentClass.startsWith('!')) {
          predicates << new Predicate<Repository>() {
            @Override
            public boolean apply(@Nullable final Repository input) {
              return input && (input.repositoryContentClass.id == contentClass)
            }
          }
        }
      }
      if (!predicates.empty) {
        if (predicates.size() == 1) {
          return predicates[0]
        }
        return Predicates.or(predicates)
      }
    }
    return null
  }

  private static Predicate<Repository> hasNoneOfContentClasses(final String[] contentClasses) {
    if (contentClasses) {
      List<Predicate<Repository>> predicates = []
      contentClasses.each { String contentClass ->
        if (StringUtils.isNotEmpty(contentClass) && contentClass.startsWith('!')) {
          predicates << new Predicate<Repository>() {
            @Override
            public boolean apply(@Nullable final Repository input) {
              return input != null && !(input.repositoryContentClass.id == contentClass.substring(1))
            }
          }
        }
      }
      if (!predicates.empty) {
        if (predicates.size() == 1) {
          return predicates[0]
        }
        return Predicates.or(predicates)
      }
    }
    return null
  }

}
