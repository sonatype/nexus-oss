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
package com.sonatype.nexus.ssl.plugin.internal.repository;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.capability.Condition;
import org.sonatype.nexus.capability.Tag;
import org.sonatype.nexus.capability.Taggable;
import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.capability.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ssl.model.RepositoryTrustStoreKey.repositoryTrustStoreKey;
import static org.sonatype.nexus.capability.Tag.repositoryTag;
import static org.sonatype.nexus.capability.Tag.tags;

/**
 * Repository capability (enables Nexus SSL Trust Store / repository).
 *
 * @since ssl 1.0
 */
@Named(RepositoryCapabilityDescriptor.TYPE_ID)
public class RepositoryCapability
    extends CapabilitySupport<RepositoryCapabilityConfiguration>
    implements Taggable
{

  private final TrustStore trustStore;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("%s")
    String description(String repository);
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public RepositoryCapability(final TrustStore trustStore,
                              final RepositoryRegistry repositoryRegistry)
  {
    this.trustStore = checkNotNull(trustStore);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  @Override
  protected RepositoryCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new RepositoryCapabilityConfiguration(properties);
  }

  @Override
  protected void onActivate(final RepositoryCapabilityConfiguration config) {
    trustStore.enableFor(repositoryTrustStoreKey(config.getRepositoryId()));
  }

  @Override
  protected void onPassivate(final RepositoryCapabilityConfiguration config) {
    trustStore.disableFor(repositoryTrustStoreKey(config.getRepositoryId()));
  }

  @Override
  public Condition activationCondition() {
    return conditions().logical().and(
        conditions().nexus().active(),
        conditions().capabilities().passivateCapabilityDuringUpdate()
    );
  }

  @Override
  public Condition validityCondition() {
    return conditions().repository().repositoryExists(new RepositoryConditions.RepositoryId()
    {
      @Override
      public String get() {
        return isConfigured() ? getConfig().getRepositoryId() : null;
      }
    });
  }

  @Override
  protected String renderDescription() {
    try {
      return messages.description(repositoryRegistry.getRepository(getConfig().getRepositoryId()).getName());
    }
    catch (NoSuchRepositoryException e) {
      return messages.description(getConfig().getRepositoryId());
    }
  }

  @Override
  public Set<Tag> getTags() {
    return tags(repositoryTag(renderDescription()));
  }

}
