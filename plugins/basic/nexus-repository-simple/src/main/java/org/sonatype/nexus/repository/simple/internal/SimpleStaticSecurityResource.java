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

package org.sonatype.nexus.repository.simple.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.security.CRoleBuilder;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.SecurityModelConfiguration;
import org.sonatype.security.realms.tools.StaticSecurityResource;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.security.RepositoryFormatPrivilegeDescriptor.id;
import static org.sonatype.nexus.repository.security.RepositoryFormatPrivilegeDescriptor.privilege;

/**
 * ???
 *
 * @since 3.0
 */
@Named
@Singleton
public class SimpleStaticSecurityResource
    implements StaticSecurityResource
{
  private final Format format;

  @Inject
  public SimpleStaticSecurityResource(final @Named(SimpleFormat.NAME) Format format) {
    this.format = checkNotNull(format);
  }

  @Override
  public SecurityModelConfiguration getConfiguration() {
    String formatName = format.getValue();
    Configuration model = new Configuration();

    // add repository-format privileges
    model.addPrivilege(privilege(formatName, "browse"));
    model.addPrivilege(privilege(formatName, "read"));
    model.addPrivilege(privilege(formatName, "edit"));
    model.addPrivilege(privilege(formatName, "add"));
    model.addPrivilege(privilege(formatName, "delete"));

    // add repository-format 'admin' role
    model.addRole(new CRoleBuilder()
        .id(String.format("repository-format-%s-admin", formatName))
        .privilege(id(formatName, "browse"))
        .privilege(id(formatName, "read"))
        .privilege(id(formatName, "edit"))
        .privilege(id(formatName, "add"))
        .privilege(id(formatName, "delete"))
        .create());

    // add repository-format 'readonly' role
    model.addRole(new CRoleBuilder()
        .id(String.format("repository-format-%s-readonly", formatName))
        .privilege(id(formatName, "browse"))
        .privilege(id(formatName, "read"))
        .create());

    // add repository-format 'deployer' role
    model.addRole(new CRoleBuilder()
        .id(String.format("repository-format-%s-readonly", formatName))
        .privilege(id(formatName, "browse"))
        .privilege(id(formatName, "read"))
        .privilege(id(formatName, "edit"))
        .privilege(id(formatName, "add"))
        .create());

    return model;
  }
}
