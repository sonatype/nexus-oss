/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "userAndGroupConfig" )
public class LdapUserAndGroupConfigurationResponse
{

    private LdapUserAndGroupConfigurationDTO data;

    /**
     * @return the userAndGroupConfig
     */
    public LdapUserAndGroupConfigurationDTO getData()
    {
        return data;
    }

    /**
     * @param userAndGroupConfig the userAndGroupConfig to set
     */
    public void setData( LdapUserAndGroupConfigurationDTO userAndGroupConfig )
    {
        this.data = userAndGroupConfig;
    }
    
    
}
