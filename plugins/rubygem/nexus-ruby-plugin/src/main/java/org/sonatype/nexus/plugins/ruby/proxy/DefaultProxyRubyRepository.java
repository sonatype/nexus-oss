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
package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.NexusRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.layout.ProxiedRubygemsFileSystem;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@Named(DefaultProxyRubyRepository.ID)
public class DefaultProxyRubyRepository
    extends AbstractProxyRepository
    implements ProxyRubyRepository, Repository
{
  public static final String ID = "rubygems-proxy";

  private final ContentClass contentClass;

  private final ProxyRubyRepositoryConfigurator configurator;

  private final RubygemsGateway gateway;

  private final RepositoryKind repositoryKind;

  private NexusRubygemsFacade facade;

  @Inject
  public DefaultProxyRubyRepository(@Named(RubyContentClass.ID) ContentClass contentClass,
                                    ProxyRubyRepositoryConfigurator configurator,
                                    RubygemsGateway gateway)
      throws LocalStorageException, ItemNotFoundException
  {
    this.contentClass = contentClass;
    this.configurator = configurator;
    this.gateway = gateway;
    this.repositoryKind = new DefaultRepositoryKind(ProxyRubyRepository.class,
        Arrays.asList(new Class<?>[]{RubyRepository.class}));
    this.facade = new NexusRubygemsFacade(new ProxiedRubygemsFileSystem(gateway, new ProxyNexusStorage(this)));
  }

  @Override
  protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator() {
    return configurator;
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<DefaultProxyRubyRepositoryConfiguration>()
    {
      public DefaultProxyRubyRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new DefaultProxyRubyRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

  public RepositoryKind getRepositoryKind() {
    return repositoryKind;
  }

  @Override
  protected DefaultProxyRubyRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (DefaultProxyRubyRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @Override
  protected boolean isOld(StorageItem item) {
    if (item.getName().contains("specs.4.8")) {
      // whenever there is retrieve call to the ungzipped file it will be forwarded to call for the gzipped file
      return false;
    }
    if (item.getName().endsWith(".gz")) {
      if (log.isDebugEnabled()) {
        log.debug("{} needs remote update {} ", item, isOld(getMetadataMaxAge(), item));
      }
      return isOld(getMetadataMaxAge(), item);
    }
    if (item.getName().endsWith(".json.rz")) {
      if (log.isDebugEnabled()) {
        log.debug("{} needs remote update {}", item, isOld(getMetadataMaxAge(), item));
      }
      if (isOld(getMetadataMaxAge(), item)) {
        // avoid sending a wrong HEAD request which does not trigger the expiration
        try {
          super.deleteItem(false, item.getResourceStoreRequest());
        }
        catch (@SuppressWarnings("deprecation") StorageException
            | UnsupportedStorageOperationException | IllegalOperationException | ItemNotFoundException e) {
          log.error("could not delete volatile file: " + item);
        }
        return true;
      }
      return false;
    }
    else {
      // all other files use artifact max age
      return isOld(getExternalConfiguration(false).getArtifactMaxAge(), item);
    }
  }

  public int getArtifactMaxAge() {
    return getExternalConfiguration(false).getArtifactMaxAge();
  }

  public void setArtifactMaxAge(int maxAge) {
    getExternalConfiguration(true).setArtifactMaxAge(maxAge);
  }

  public int getMetadataMaxAge() {
    return getExternalConfiguration(false).getMetadataMaxAge();
  }

  public void setMetadataMaxAge(int metadataMaxAge) {
    getExternalConfiguration(true).setMetadataMaxAge(metadataMaxAge);
  }

  private static Pattern BUNDLER_API_REQUEST = Pattern.compile("[?]gems=.+,.+");

  public AbstractStorageItem doCacheItem(AbstractStorageItem item)
      throws LocalStorageException
  {
    // a request for single gem will be cached but one for many will not be cached
    if (BUNDLER_API_REQUEST.matcher(item.getPath()).matches()) {
      return item;
    }
    else {
      return super.doCacheItem(item);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  protected AbstractStorageItem doRetrieveRemoteItem(ResourceStoreRequest request)
      throws ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
  {
    RubygemsFile file = facade.file(request.getRequestPath());

    // make the remote request with the respective remote path
    request.setRequestPath(file.remotePath());
    return super.doRetrieveRemoteItem(request);
  }

  @Override
  public RepositoryItemUid createUid(final String path) {
    RubygemsFile file = facade.file(path);
    if (file.type() == FileType.NOT_FOUND) {
      // nexus internal path like .nexus/**/*
      return super.createUid(path);
    }
    return super.createUid(file.storagePath());
  }

  @Override
  public void moveItem(ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException
  {
    throw new UnsupportedStorageOperationException(from.getRequestPath());
  }

  @SuppressWarnings("deprecation")
  @Override
  public StorageItem retrieveDirectItem(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, IOException
  {
    // bypass access control
    return super.retrieveItem(false, request);
  }

  @SuppressWarnings("deprecation")
  @Override
  public StorageItem retrieveItem(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    // TODO do not use this since it bypasses access control
    if (request.getRequestPath().startsWith("/.nexus")) {
      return super.retrieveItem(request);
    }

    return facade.handleRetrieve(this, request, facade.get(request));
  }

  @SuppressWarnings("deprecation")
  @Override
  public StorageItem retrieveItem(boolean fromTask, ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException
  {
    if (!fromTask && request.getRequestPath().contains("?gems=") && !request.getRequestPath().startsWith("/.nexus")) {
      return facade.handleRetrieve(this, request, facade.get(request));
    }
    else {
      return super.retrieveItem(fromTask, request);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void syncMetadata() throws IllegalOperationException, ItemNotFoundException, org.sonatype.nexus.proxy.StorageException
  {
    for (SpecsIndexType type : SpecsIndexType.values()) {
      ResourceStoreRequest request = new ResourceStoreRequest(type.filepathGzipped());
      request.setRequestRemoteOnly(true);
      retrieveItem(true, request);
    }
    String directory = getBaseDirectory();
    gateway.purgeBrokenDepencencyFiles(directory);
    gateway.purgeBrokenGemspecFiles(directory);
  }

  private String getBaseDirectory() throws ItemNotFoundException, LocalStorageException {
    String basedir = this.getLocalUrl().replace("file:", "");
    if (log.isDebugEnabled()) {
      log.debug("recreate rubygems metadata in {}", basedir);
    }
    return basedir;
  }
}
