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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;
import org.sonatype.nexus.proxy.repository.GroupRepository;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.yum.internal.capabilities.MergeMetadataCapabilityConfiguration.REPOSITORY_ID;

/**
 * @since yum 3.0
 */
@Singleton
@Named(MergeMetadataCapabilityDescriptor.TYPE_ID)
public class MergeMetadataCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements CapabilityDescriptor
{

  public static final String TYPE_ID = "yum.merge";

  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final Validators validators;

  @Inject
  public MergeMetadataCapabilityDescriptor(final Validators validators) {
    super(
        TYPE,
        "Yum: Merge Metadata",
        "Merges Yum metadata from group members.",
        new RepoOrGroupComboFormField(REPOSITORY_ID, FormField.MANDATORY)
    );
    this.validators = validators;
  }

  @Override
  public Validator validator() {
    return validators.logical().and(
        validators.repository().repositoryOfType(TYPE, REPOSITORY_ID, GroupRepository.class),
        validators.capability().uniquePer(TYPE, REPOSITORY_ID)
    );
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators.logical().and(
        validators.repository().repositoryOfType(TYPE, REPOSITORY_ID, GroupRepository.class),
        validators.capability().uniquePerExcluding(id, TYPE, REPOSITORY_ID)
    );
  }

}
