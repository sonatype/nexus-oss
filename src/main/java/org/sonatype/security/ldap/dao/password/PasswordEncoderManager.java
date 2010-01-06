/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;


/**
 * @author cstamas
 */
public interface PasswordEncoderManager
{

    public String encodePassword( String password, Object salt );

    public boolean isPasswordValid( String encodedPassword, String password, Object salt );

    public String getPreferredEncoding();

    public void setPreferredEncoding( String preferredEncoding );

}
