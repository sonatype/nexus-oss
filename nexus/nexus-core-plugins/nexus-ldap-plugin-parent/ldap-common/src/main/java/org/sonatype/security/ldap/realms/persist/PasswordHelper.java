/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import org.sonatype.security.ldap.upgrade.cipher.PlexusCipherException;

public interface PasswordHelper
{

    public String encrypt( String password )
        throws PlexusCipherException;

    public String decrypt( String encodedPassword )
        throws PlexusCipherException;

}
