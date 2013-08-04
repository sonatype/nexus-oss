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

package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration.REPOSITORY;
import static org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2MetadataGeneratorCapabilityDescriptor.TYPE_ID;

@Singleton
@Named(TYPE_ID)
public class P2MetadataGeneratorCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements CapabilityDescriptor
{

  public static final String TYPE_ID = "p2.repository.metadata.generator";

  private static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final Validators validators;

  @Inject
  public P2MetadataGeneratorCapabilityDescriptor(final Validators validators) {
    super(
        TYPE,
        "P2 Metadata Generator capability",
        "Automatically generates P2 metadata for OSGi bundles deployed to selected repository\n"
            + "<br/>\n"
            + "<br/>\n"
            + "<span style=\"font-weight: bold;\">EXPERIMENTAL</span>\n"
            + "<br/>"
            + "This is an experimental, unsupported feature.",
        new RepoOrGroupComboFormField(REPOSITORY, FormField.MANDATORY)
    );
    this.validators = checkNotNull(validators);
  }

  /**
   * Validate on create that there is only one capability for the configured repository.
   *
   * @since 2.3.1
   */
  @Override
  public Validator validator() {
    return validators.capability().uniquePer(TYPE, REPOSITORY);
  }

  /**
   * Validate on update that there is only one capability for the configured repository.
   *
   * @since 2.3.1
   */
  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators.capability().uniquePerExcluding(id, TYPE, REPOSITORY);
  }

}
