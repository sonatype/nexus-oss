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
package com.sonatype.nexus.ldap.model;

import com.sonatype.nexus.ssl.model.TrustStoreKey;

/**
 * LDAP {@link TrustStoreKey}.
 *
 * @since 2.4
 */
public class LdapTrustStoreKey
    extends TrustStoreKey
{

  public static final String TYPE = "ldap";

  public LdapTrustStoreKey(final String ldapServerId) {
    super(TYPE + "/" + checkNotNull(ldapServerId, "LdapServerId cannot be null"));
  }

  public static LdapTrustStoreKey ldapTrustStoreKey(final String ldapServerId) {
    return new LdapTrustStoreKey(ldapServerId);
  }

}
