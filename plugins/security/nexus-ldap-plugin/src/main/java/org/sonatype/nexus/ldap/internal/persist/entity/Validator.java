/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.persist.entity;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * LDAP Server configuration validator.
 *
 * @since 3.0
 */
@Singleton
@Named
public final class Validator
{
  /**
   * Validates the {@link LdapConfiguration}. ID is not validated, it is always set on create.
   */
  public void validate(final LdapConfiguration ldapConfiguration) throws IllegalArgumentException {
    checkArgument(!Strings.isNullOrEmpty(ldapConfiguration.getName()), "name");
    checkArgument(ldapConfiguration.getConnection() != null, "connection");
    checkArgument(ldapConfiguration.getMapping() != null, "mapping");

    validate(ldapConfiguration.getConnection());
    validate(ldapConfiguration.getMapping());
  }

  /**
   * Validates {@link Connection}.
   */
  public void validate(final Connection connection) throws IllegalArgumentException {
    checkNotNull(connection);
    checkArgument(connection.getHost() != null, "connection.host");
    checkArgument(!Strings.isNullOrEmpty(connection.getAuthScheme()), "connection.authScheme");
    checkArgument(!Strings.isNullOrEmpty(connection.getSearchBase()), "connection.searchBase");
    if (!Strings.isNullOrEmpty(connection.getAuthScheme())
        && !"none".equals(connection.getAuthScheme())) {
      checkArgument(!Strings.isNullOrEmpty(connection.getSystemUsername()) &&
          !Strings.isNullOrEmpty(connection.getSystemPassword()), "connection.systemUsername");
    }
  }

  /**
   * Validates {@link Mapping}.
   */
  public void validate(final Mapping mapping) throws IllegalArgumentException {
    checkNotNull(mapping);
    checkArgument(!Strings.isNullOrEmpty(mapping.getUserObjectClass()), "mapping.userObjectClass");
    checkArgument(!Strings.isNullOrEmpty(mapping.getUserIdAttribute()), "mapping.userIdAttribute");
    checkArgument(!Strings.isNullOrEmpty(mapping.getUserRealNameAttribute()), "mapping.userRealNameAttribute");
    checkArgument(!Strings.isNullOrEmpty(mapping.getEmailAddressAttribute()), "mapping.emailAddressAttribute");
    if (mapping.isLdapGroupsAsRoles()
        && Strings.isNullOrEmpty(mapping.getUserMemberOfAttribute())) {
      checkArgument(!Strings.isNullOrEmpty(mapping.getGroupIdAttribute()), "mapping.groupIdAttribute");
      checkArgument(!Strings.isNullOrEmpty(mapping.getGroupMemberAttribute()), "mapping.groupMemberAttribute");
      checkArgument(!Strings.isNullOrEmpty(mapping.getGroupMemberFormat()), "mapping.groupMemberFormat");
      checkArgument(!Strings.isNullOrEmpty(mapping.getGroupObjectClass()), "mapping.groupObjectClass");
    }
  }
}
