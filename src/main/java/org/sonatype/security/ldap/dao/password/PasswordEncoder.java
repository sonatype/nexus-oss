/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;


public interface PasswordEncoder
{

    String ROLE = PasswordEncoder.class.getName();
    
    String getMethod();

    String encodePassword( String password, Object salt );

    boolean isPasswordValid( String encPassword, String inputPassword, Object salt );

}
