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
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

/**
 * @since yum 3.0
 */
@Singleton
@Named(YumCapabilityDescriptor.TYPE_ID)
public class YumCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements CapabilityDescriptor
{

  public static final String TYPE_ID = "yum";

  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final Validators validators;

  @Inject
  public YumCapabilityDescriptor(final Validators validators) {
    super(
        TYPE,
        "Yum: Configuration",
        "Yum plugin configuration.",
        new NumberTextFormField(
            YumCapabilityConfiguration.MAX_NUMBER_PARALLEL_THREADS,
            "Max number of parallel threads",
            "Maximum number of threads to be used for generating Yum repositories"
                + " (default 10 threads)",
            FormField.OPTIONAL
        ).withInitialValue(10)
    );
    this.validators = validators;
  }

  @Override
  public Validator validator() {
    return validators.logical().and(
        validators.capability().uniquePer(TYPE)
    );
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators.logical().and(
        validators.capability().uniquePerExcluding(id, TYPE)
    );
  }

}
