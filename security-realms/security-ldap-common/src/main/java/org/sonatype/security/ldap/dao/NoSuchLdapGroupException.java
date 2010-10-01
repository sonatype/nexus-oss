/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

public class NoSuchLdapGroupException
    extends Exception
{

    private final String groupId;

    private final String logicalGroupId;

    public NoSuchLdapGroupException( String groupId, String logicalGroupId )
    {
        super( "No such group: " + groupId + " (logical name: " + logicalGroupId + ")" );

        this.groupId = groupId;
        this.logicalGroupId = logicalGroupId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getLogicalGroupId()
    {
        return logicalGroupId;
    }

}
