/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import org.sonatype.security.ldap.dao.LdapAuthConfiguration;

import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

public interface LdapConfiguration
{


    void save();

    void clearCache();
    
    // connection info
    
    CConnectionInfo readConnectionInfo();
    
    void updateConnectionInfo( CConnectionInfo connectionInfo ) throws InvalidConfigurationException;

    
    // user and group info
    
    CUserAndGroupAuthConfiguration readUserAndGroupConfiguration();

    void updateUserAndGroupConfiguration( CUserAndGroupAuthConfiguration userAndGroupConf ) throws InvalidConfigurationException;
    
    LdapAuthConfiguration getLdapAuthConfiguration();
    
}
