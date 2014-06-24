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
package com.sonatype.nexus.ssl.plugin.internal.smtp;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ssl.plugin.spi.CapabilityManager;

import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder.capabilities;

/**
 * {@link SMTPCapability} manager.
 *
 * @since ssl 1.0
 */
@Named("smtp")
@Singleton
public class SMTPCapabilityManager
    implements CapabilityManager
{

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public SMTPCapabilityManager(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
  }

  @Override
  public CapabilityReference get(final String id) {
    final CapabilityReferenceFilterBuilder.CapabilityReferenceFilter filter = capabilities()
        .withType(SMTPCapabilityDescriptor.TYPE);

    final Collection<? extends CapabilityReference> capabilities = capabilityRegistry.get(filter);
    if (capabilities != null && !capabilities.isEmpty()) {
      return capabilities.iterator().next();
    }
    return null;
  }

  @Override
  public CapabilityReference enable(final String id, final boolean enabled)
      throws Exception
  {
    final CapabilityReference reference = get(id);
    if (reference == null) {
      return capabilityRegistry.add(
          SMTPCapabilityDescriptor.TYPE,
          enabled,
          null, // automatically generated notes
          null  // no configuration
      );
    }
    else {
      return capabilityRegistry.update(
          reference.context().id(),
          enabled,
          reference.context().notes(),
          reference.context().properties()
      );
    }
  }

}
