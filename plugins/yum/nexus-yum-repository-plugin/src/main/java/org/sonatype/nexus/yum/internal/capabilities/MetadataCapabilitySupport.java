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

package org.sonatype.nexus.yum.internal.capabilities;

import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;
import org.sonatype.nexus.plugins.capabilities.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.internal.YumConfigContentGenerator;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.apache.commons.io.IOUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since yum 3.0
 */
public abstract class MetadataCapabilitySupport<C extends MetadataCapabilityConfigurationSupport>
    extends CapabilitySupport
{

  private final YumRegistry yumRegistry;

  private final Conditions conditions;

  private final RepositoryRegistry repositoryRegistry;

  private C configuration;

  @Inject
  public MetadataCapabilitySupport(final YumRegistry yumRegistry,
                                   final Conditions conditions,
                                   final RepositoryRegistry repositoryRegistry)
  {
    this.yumRegistry = checkNotNull(yumRegistry);
    this.conditions = checkNotNull(conditions);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  @Override
  public String description() {
    if (isConfigured()) {
      try {
        return repositoryRegistry.getRepository(configuration.repository()).getName();
      }
      catch (NoSuchRepositoryException e) {
        return configuration.repository();
      }
    }
    return null;
  }

  @Override
  public void onCreate()
      throws Exception
  {
    configuration = createConfiguration(context().properties());
  }

  @Override
  public void onLoad()
      throws Exception
  {
    configuration = createConfiguration(context().properties());
  }

  @Override
  public void onUpdate()
      throws Exception
  {
    configuration = createConfiguration(context().properties());
    final Yum yum = yumRegistry.get(configuration.repository());
    // yum is not present when repository is changed
    if (yum != null) {
      configureYum(yum);
    }
  }

  @Override
  public void onRemove()
      throws Exception
  {
    configuration = null;
  }

  @Override
  public void onActivate() {
    try {
      final Repository repository = repositoryRegistry.getRepository(configuration.repository());
      configureYum(yumRegistry.register(repository.adaptToFacet(MavenRepository.class)));
    }
    catch (NoSuchRepositoryException e) {
      // TODO
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void onPassivate() {
    yumRegistry.unregister(configuration.repository());
  }

  @Override
  public Condition activationCondition() {
    return conditions.logical().and(
        conditions.capabilities().capabilityOfTypeActive(YumCapabilityDescriptor.TYPE),
        conditions.repository().repositoryIsInService(new RepositoryConditions.RepositoryId()
        {
          @Override
          public String get() {
            return isConfigured() ? configuration.repository() : null;
          }
        }),
        conditions.capabilities().passivateCapabilityWhenPropertyChanged(
            MetadataCapabilityConfigurationSupport.REPOSITORY_ID
        )
    );
  }

  @Override
  public Condition validityCondition() {
    return conditions.repository().repositoryExists(new RepositoryConditions.RepositoryId()
    {
      @Override
      public String get() {
        return isConfigured() ? configuration.repository() : null;
      }
    });
  }

  @Override
  public String status() {
    if (isConfigured()) {
      try {
        final Repository repository = repositoryRegistry.getRepository(configuration.repository());
        final StorageItem storageItem = repository.retrieveItem(
            new ResourceStoreRequest(YumConfigContentGenerator.configFilePath(repository.getId()), true)
        );
        if (storageItem instanceof StorageFileItem) {
          InputStream in = null;
          try {
            in = ((StorageFileItem) storageItem).getInputStream();
            return
                "<b>Example Yum configuration file:</b><br/><br/>"
                    + "<pre>"
                    + IOUtils.toString(in)
                    + "</pre>";
          }
          finally {
            Closeables.closeQuietly(in);
          }
        }
      }
      catch (Exception e) {
        return super.status();
      }
    }
    return null;
  }

  public C configuration() {
    return configuration;
  }

  void configureYum(final Yum yum) {
    // template method
  }

  abstract C createConfiguration(final Map<String, String> properties);

  boolean isConfigured() {
    return configuration != null;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + (isConfigured() ? "{repository=" + configuration.repository() + "}" : "");
  }

}
