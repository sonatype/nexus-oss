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
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.proxy.ResourceStoreRequest
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.maven.MavenHostedRepository
import org.sonatype.nexus.proxy.maven.MavenProxyRepository
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry
import org.sonatype.nexus.proxy.repository.*
import org.sonatype.nexus.rapture.TrustStoreKeys
import org.sonatype.nexus.rest.RepositoryURLBuilder
import org.sonatype.nexus.templates.TemplateManager
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider
import org.sonatype.nexus.templates.repository.RepositoryTemplate

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
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
  RepositoryRegistry repositoryRegistry

  @Inject
  RepositoryTypeRegistry repositoryTypeRegistry

  @Inject
  RepositoryURLBuilder repositoryURLBuilder

  @Inject
  TemplateManager templateManager

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  DefaultRepositoryTemplateProvider repositoryTemplateProvider

  @Inject
  @Nullable
  TrustStoreKeys trustStoreKeys

  private def typesToClass = [
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
   * Retrieve a list of available repository templates.
   */
  @DirectMethod
  @RequiresPermissions('nexus:componentsrepotypes:read')
  List<RepositoryTemplateXO> readTemplates(final @Nullable StoreLoadParameters parameters) {
    def providers = []
    def asProvider = { template, type, masterFormat ->
      new RepositoryTemplateXO(
          id: template.id,
          type: type,
          provider: template.repositoryProviderHint,
          providerName: template.description,
          format: template.contentClass.id,
          formatName: template.contentClass.name,
          masterFormat: masterFormat
      )
    }
    def types = typesToClass
    templateManager.templates.getTemplates(RepositoryTemplate.class).templatesList.each {
      def template = it as RepositoryTemplate
      types.each {
        if (template.targetFits(it.value)) {
          def masterFormat = null,
              type = it.key == 'shadow' ? 'virtual' : it.key,
              format = template.contentClass.id

          if (type == 'virtual' && template.contentClass.id.startsWith('maven')) {
            masterFormat = template.contentClass.id == 'maven1' ? 'maven2' : 'maven1'
          }

          def typeFilter = parameters?.getFilter('type')
          def formatFilter = parameters?.getFilter('format')
          if ((type == (typeFilter ?: type) && (format == (formatFilter ?: format)))) {
            providers.add(asProvider(template, type, masterFormat))
          }
        }
      }
    }
    return providers
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
  RepositoryXO createGroup(final RepositoryGroupXO repositoryXO) {
    create(repositoryXO, doUpdateGroup)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateGroup(final RepositoryGroupXO repositoryXO) {
    update(repositoryXO, GroupRepository.class, doUpdateGroup)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  RepositoryXO createHosted(final RepositoryHostedXO repositoryXO) {
    create(repositoryXO, doUpdateHosted)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateHosted(final RepositoryHostedXO repositoryXO) {
    update(repositoryXO, HostedRepository.class, doUpdateHosted)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  RepositoryXO createHostedMaven(final RepositoryHostedMavenXO repositoryXO) {
    create(repositoryXO, doUpdateHosted, doUpdateHostedMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateHostedMaven(final RepositoryHostedMavenXO repositoryXO) {
    update(repositoryXO, MavenHostedRepository.class, doUpdateHosted, doUpdateHostedMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  RepositoryXO createProxy(final RepositoryProxyXO repositoryXO) {
    create(repositoryXO, doUpdateProxy)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateProxy(final RepositoryProxyXO repositoryXO) {
    update(repositoryXO, ProxyRepository.class, doUpdateProxy)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  RepositoryXO createProxyMaven(final RepositoryProxyMavenXO repositoryXO) {
    create(repositoryXO, doUpdateProxy, doUpdateProxyMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateProxyMaven(final RepositoryProxyMavenXO repositoryXO) {
    update(repositoryXO, MavenProxyRepository.class, doUpdateProxy, doUpdateProxyMaven)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:create')
  RepositoryXO createVirtual(final RepositoryVirtualXO repositoryXO) {
    create(repositoryXO, [doCreateVirtual], [doUpdateVirtual])
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  RepositoryXO updateVirtual(final RepositoryVirtualXO repositoryXO) {
    update(repositoryXO, ShadowRepository.class, doUpdateVirtual)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:delete')
  void delete(final String id) {
    repositoryRegistry.removeRepository(id)
  }

  def RepositoryXO create(RepositoryXO repositoryXO, Closure... updateClosures) {
    return create(repositoryXO, [], updateClosures as List)
  }

  def RepositoryXO create(RepositoryXO repositoryXO, List<Closure> createClosures, List<Closure> updateClosures) {
    def template = templateManager.templates.getTemplateById(repositoryXO.template) as RepositoryTemplate
    ConfigurableRepository configurable = template.getConfigurableRepository().with {
      id = repositoryXO.id
      name = repositoryXO.name
      exposed = repositoryXO.exposed
      localStatus = LocalStatus.IN_SERVICE
      return it
    }
    createClosures.each { createClosure ->
      createClosure(configurable, repositoryXO)
    }
    Repository created = template.create()
    updateClosures.each { updateClosure ->
      updateClosure(created, repositoryXO)
    }
    nexusConfiguration.saveConfiguration()
    return asRepositoryXO(created, readTemplates(null))
  }

  def <T extends Repository> RepositoryXO update(RepositoryXO repositoryXO, Class<T> repoType, Closure... updateClosures) {
    if (repositoryXO.id) {
      T updated = repositoryRegistry.getRepositoryWithFacet(repositoryXO.id, repoType).with {
        name = repositoryXO.name
        exposed = repositoryXO.exposed
        return it
      }
      updateClosures.each { updateClosure ->
        updateClosure(updated, repositoryXO)
      }
      nexusConfiguration.saveConfiguration()
      return asRepositoryXO(updated, readTemplates(null))
    }
    throw new IllegalArgumentException('Missing id for repository to be updated')
  }

  def static doUpdateGroup = { GroupRepository repo, RepositoryGroupXO repositoryXO ->
    repo.memberRepositoryIds = repositoryXO.memberRepositoryIds
  }

  def static doUpdateHosted = { HostedRepository repo, RepositoryHostedXO repositoryXO ->
    repo.browseable = repositoryXO.browseable
    repo.writePolicy = repositoryXO.writePolicy
  }

  def static doUpdateHostedMaven = { MavenHostedRepository repo, RepositoryHostedMavenXO repositoryXO ->
    repo.indexable = repositoryXO.indexable
  }

  def doUpdateProxy = { ProxyRepository repo, RepositoryProxyXO repositoryXO ->
    repo.browseable = repositoryXO.browseable
    repo.remoteUrl = repositoryXO.remoteStorageUrl
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
    if (repositoryXO.httpRequestSettings) {
      repo.remoteConnectionSettings = new DefaultRemoteConnectionSettings(
          userAgentCustomizationString: repositoryXO.userAgentCustomisation,
          queryString: repositoryXO.urlParameters,
          connectionTimeout: repositoryXO.timeout * 1000,
          retrievalRetryCount: repositoryXO.retries
      )
    }
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, repo.id, repositoryXO.useTrustStoreForRemoteStorageUrl)
  }

  def static doUpdateProxyMaven = { MavenProxyRepository repo, RepositoryProxyMavenXO repositoryXO ->
    repo.downloadRemoteIndexes = repositoryXO.downloadRemoteIndexes
    if (repositoryXO.checksumPolicy != null) repo.checksumPolicy = repositoryXO.checksumPolicy
    if (repositoryXO.artifactMaxAge != null) repo.artifactMaxAge = repositoryXO.artifactMaxAge
    if (repositoryXO.metadataMaxAge != null) repo.metadataMaxAge = repositoryXO.metadataMaxAge
  }

  def static doCreateVirtual = { ConfigurableRepository repo, RepositoryVirtualXO repositoryXO ->
    // HACK: we should not use a Maven2->Maven1 config
    M2LayoutedM1ShadowRepositoryConfiguration exConf = new M2LayoutedM1ShadowRepositoryConfiguration(
        repo.getCurrentConfiguration(true).externalConfiguration as Xpp3Dom
    )
    exConf.masterRepositoryId = repositoryXO.shadowOf
    exConf.synchronizeAtStartup = repositoryXO.synchronizeAtStartup
  }

  def doUpdateVirtual = { ShadowRepository repo, RepositoryVirtualXO repositoryXO ->
    repo.synchronizeAtStartup = repositoryXO.synchronizeAtStartup
    repo.masterRepository = repositoryRegistry.getRepository(repositoryXO.shadowOf)
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
        timeout = rcs?.connectionTimeout == 0 ? 0 : rcs?.connectionTimeout / 1000
        retries = rcs?.retrievalRetryCount
        httpRequestSettings = userAgentCustomisation || urlParameters || timeout || retries
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
        repo.getRemoteStatus(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT), false)?.with { status ->
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
        url = repositoryURLBuilder.getExposedRepositoryContentUrl(repo)
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
    List<Repository> repositories = repositoryRegistry.repositories
    boolean includeUserManaged = true
    boolean includeNexusManaged = false
    if (parameters) {
      def typeFilter = parameters.getFilter('type')
      if (typeFilter) {
        def clazz = typesToClass[typeFilter]
        if (!clazz) {
          throw new IllegalArgumentException('Repository type not supported: ' + typeFilter)
        }
        repositories = repositoryRegistry.getRepositoriesWithFacet(clazz)
      }
      def formatFilter = parameters.getFilter('format')
      if (formatFilter) {
        repositories = repositories.findResults { Repository repository ->
          repository.repositoryContentClass.id == formatFilter ? repository : null
        }
      }
      def includeUserManagedFilter = parameters.getFilter('includeUserManaged')
      if (includeUserManagedFilter != null) {
        includeUserManaged = Boolean.valueOf(includeUserManagedFilter)
      }
      def includeNexusManagedFilter = parameters.getFilter('includeNexusManaged')
      if (includeNexusManagedFilter != null) {
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
    return repositories
  }

}
