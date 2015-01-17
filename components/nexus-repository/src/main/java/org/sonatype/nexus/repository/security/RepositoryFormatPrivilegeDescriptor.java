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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;

/**
 * Repository format {@link PrivilegeDescriptor}.
 *
 * @since 3.0
 */
@Named(RepositoryFormatPrivilegeDescriptor.TYPE)
@Singleton
public class RepositoryFormatPrivilegeDescriptor
    extends PrivilegeDescriptorSupport
{
  public static final String TYPE = "repository-format";

  public static final String P_FORMAT = "format";

  public static final String P_ACTIONS = "actions";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getName() {
    return "Repository Format";
  }

  @Override
  protected String formatPermission(final CPrivilege privilege) {
    String formatName = readProperty(privilege, P_FORMAT, "*");
    String actions = readProperty(privilege, P_ACTIONS, "*");
    return permission(formatName, actions);
  }

  //
  // Helpers
  //

  public static String id(final String formatName, final String actions) {
    return String.format("%s-%s-%s", TYPE, formatName, actions);
  }

  public static String permission(final String formatName, final String actions) {
    return String.format("nexus:%s:%s:%s", TYPE, formatName, actions);
  }

  public static CPrivilege privilege(final String formatName, final String actions) {
    CPrivilege privilege = new CPrivilege();
    privilege.setType(TYPE);
    privilege.setId(id(formatName, actions));
    privilege.setName(permission(formatName, actions));
    privilege.setDescription(String.format("Grants '%s' repository format actions: %s", formatName, actions));
    privilege.setProperty(P_FORMAT, formatName);
    privilege.setProperty(P_ACTIONS, actions);
    return privilege;
  }
}
