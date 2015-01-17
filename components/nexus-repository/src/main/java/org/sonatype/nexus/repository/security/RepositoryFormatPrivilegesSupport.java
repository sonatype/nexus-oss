/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.security;

import org.sonatype.nexus.repository.Format;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.realms.tools.StaticSecurityResource;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.security.RepositoryFormatPrivilegeDescriptor.privilege;

// FIXME: Rename, this is not just privs, but any security configuration?

/**
 * Helper to build {@code repository-format} permissions for a given format.
 *
 * @since 3.0
 */
public class RepositoryFormatPrivilegesSupport
    implements StaticSecurityResource
{
  private final Format format;

  public RepositoryFormatPrivilegesSupport(final Format format) {
    this.format = checkNotNull(format);
  }

  // TODO: Do we add any roles by default?
  // TODO: Could potentially make this a default service, change to dynamic
  // TODO: ... and then each format doesn't need this boiler-plate,
  // TODO: ... but side effect is they can't change the defaults

  @Override
  public SecurityModelConfiguration getConfiguration() {
    String formatName = format.getValue();
    Configuration configuration = new Configuration();
    configuration.addPrivilege(privilege(formatName, "browse"));
    configuration.addPrivilege(privilege(formatName, "read"));
    configuration.addPrivilege(privilege(formatName, "edit"));
    configuration.addPrivilege(privilege(formatName, "add"));
    configuration.addPrivilege(privilege(formatName, "delete"));
    return configuration;
  }
}
