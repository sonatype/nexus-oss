/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import junit.framework.Assert;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;


public class LdapUserGroupConfValidationTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup(
            PlexusResource.class,
            "LdapUserAndGroupsConfigurationPlexusResource" );
    }

    private LdapUserAndGroupConfigurationDTO getPopulatedDTO()
    {
        LdapUserAndGroupConfigurationDTO userGroupConf = new LdapUserAndGroupConfigurationDTO();
        userGroupConf.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        userGroupConf.setGroupObjectClass( "groupOfUniqueNames" );
        userGroupConf.setGroupBaseDn( "ou=groups" );
        userGroupConf.setGroupIdAttribute( "cn" );
        userGroupConf.setGroupMemberAttribute( "uniqueMember" );
        userGroupConf.setUserObjectClass( "inetOrgPerson" );
        userGroupConf.setUserBaseDn( "ou=people" );
        userGroupConf.setUserIdAttribute( "uid" );
        userGroupConf.setUserPasswordAttribute( "userPassword" );
        userGroupConf.setUserRealNameAttribute( "cn" );
        userGroupConf.setEmailAddressAttribute( "mail" );
        return userGroupConf;
    }

    public void testNoUserBaseDn()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapUserAndGroupConfigurationResponse response = new LdapUserAndGroupConfigurationResponse();
        LdapUserAndGroupConfigurationDTO userGroupConf = this.getPopulatedDTO();
        response.setData( userGroupConf );

        userGroupConf.setUserBaseDn( null );

        LdapUserAndGroupConfigurationResponse result = (LdapUserAndGroupConfigurationResponse) resource.put( null, null, null, response );
        // make sure its null,
        Assert.assertNull( result.getData().getUserBaseDn() );
    }

    public void testNoGroupBaseDn()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapUserAndGroupConfigurationResponse response = new LdapUserAndGroupConfigurationResponse();
        LdapUserAndGroupConfigurationDTO userGroupConf = this.getPopulatedDTO();
        response.setData( userGroupConf );

        userGroupConf.setGroupBaseDn( null );

        LdapUserAndGroupConfigurationResponse result = (LdapUserAndGroupConfigurationResponse) resource.put( null, null, null, response );
        // make sure its null,
        Assert.assertNull( result.getData().getGroupBaseDn() );

    }
    
    public void testNoUserIdAttrib()
    throws Exception
{
        PlexusResource resource = getResource();

    LdapUserAndGroupConfigurationResponse response = new LdapUserAndGroupConfigurationResponse();
    LdapUserAndGroupConfigurationDTO userGroupConf = this.getPopulatedDTO();
    response.setData( userGroupConf );

    userGroupConf.setUserIdAttribute( null );

    try
    {
        resource.put( null, null, null, response );
        Assert.fail( "Expected PlexusResourceException" );
    }
    catch ( PlexusResourceException e )
    {
        ErrorResponse result = (ErrorResponse) e.getResultObject();
        Assert.assertEquals( 1, result.getErrors().size() );
        Assert.assertTrue(
            "Expected error to have the work 'user', was: " + this.getErrorString( result, 0 ),
            ( this.getErrorString( result, 0 ).toString().toLowerCase().contains( "user" ) ) );
    }

}

    public void testMultipleErrors()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapUserAndGroupConfigurationResponse response = new LdapUserAndGroupConfigurationResponse();
        LdapUserAndGroupConfigurationDTO userGroupConf = this.getPopulatedDTO();
        response.setData( userGroupConf );

        userGroupConf.setUserIdAttribute( null );
        userGroupConf.setEmailAddressAttribute( null );
        try
        {
            resource.put( null, null, null, response );
            Assert.fail( "Expected PlexusResourceException" );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse result = (ErrorResponse) e.getResultObject();
            Assert.assertEquals( 2, result.getErrors().size() );
        }
    }

}
