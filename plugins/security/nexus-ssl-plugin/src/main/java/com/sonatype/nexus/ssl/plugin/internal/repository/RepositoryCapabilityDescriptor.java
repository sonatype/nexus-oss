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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ssl.plugin.SSLPlugin;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.Tag.categoryTag;
import static org.sonatype.nexus.plugins.capabilities.Tag.tags;

/**
 * {@link RepositoryCapability} descriptor.
 *
 * @since ssl 1.0
 */
@Named(RepositoryCapabilityDescriptor.TYPE_ID)
@Singleton
public class RepositoryCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{

  /**
   * {@link RepositoryCapability} type ID (ssl.key.repository)
   */
  public static final String TYPE_ID = SSLPlugin.ID_PREFIX + ".key.repository";

  /**
   * {@link RepositoryCapability} type
   */
  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("SSL: Repository")
    String name();

    @DefaultMessage("Repository")
    String repositoryLabel();

    @DefaultMessage("Select a repository to enable Nexus SSL Trust Store for")
    String repositoryHelp();

  }

  private static final Messages messages = I18N.create(Messages.class);

  private final FormField repository;

  @Inject
  public RepositoryCapabilityDescriptor() {
    this.repository = new RepositoryCombobox(
        RepositoryCapabilityConfiguration.REPOSITORY_ID,
        messages.repositoryLabel(),
        messages.repositoryHelp(),
        MANDATORY
    ).includingAnyOfFacets(ProxyRepository.class);
  }

  @Override
  public Validator validator() {
    return validators().logical().and(
        validators().capability().uniquePer(
            RepositoryCapabilityDescriptor.TYPE, RepositoryCapabilityConfiguration.REPOSITORY_ID
        ),
        repositoryExistsAndIsAProxy()
    );
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators().logical().and(
        validators().capability().uniquePerExcluding(
            id, RepositoryCapabilityDescriptor.TYPE, RepositoryCapabilityConfiguration.REPOSITORY_ID
        ),
        repositoryExistsAndIsAProxy()
    );
  }

  private Validator repositoryExistsAndIsAProxy() {
    return validators().logical().and(
        validators().repository().repositoryOfType(
            TYPE, RepositoryCapabilityConfiguration.REPOSITORY_ID, ProxyRepository.class
        ),
        validators().repository().repositoryExists(
            TYPE, RepositoryCapabilityConfiguration.REPOSITORY_ID
        )
    );
  }

  @Override
  public CapabilityType type() {
    return TYPE;
  }

  @Override
  public String name() {
    return messages.name();
  }

  @Override
  public List<FormField> formFields() {
    return Arrays.asList(
        repository
    );
  }

  @Override
  protected String renderAbout()
      throws Exception
  {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag("Security"));
  }

}
