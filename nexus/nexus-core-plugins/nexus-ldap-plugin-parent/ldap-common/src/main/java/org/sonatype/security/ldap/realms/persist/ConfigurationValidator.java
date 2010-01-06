/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;


import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

public interface ConfigurationValidator
{   
    ValidationResponse validateConnectionInfo( ValidationContext ctx, CConnectionInfo connectionInfo );
    
    ValidationResponse validateUserAndGroupAuthConfiguration( ValidationContext ctx, CUserAndGroupAuthConfiguration userAndGroupAuthConf );
    
    ValidationResponse validateModel( ValidationRequest validationRequest );
    
}
