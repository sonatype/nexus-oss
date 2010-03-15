/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import org.sonatype.security.ldap.realms.persist.model.Configuration;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;

public class LdapUserGroupConfNotConfiguredTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapUserAndGroupsConfigurationPlexusResource" );
    }

    public void testGetNotConfigured()
        throws Exception
    {
        PlexusResource resource = getResource();

        // none of these args are used, but if they start being used, we will need to change this.
        LdapUserAndGroupConfigurationResponse response =
            (LdapUserAndGroupConfigurationResponse) resource.get( null, null, null, null );

        // the default configuration is returned.
        LdapUserAndGroupConfigurationDTO dto = response.getData();
        Assert.assertNotNull( dto );

        Assert.assertEquals( "ou=groups", dto.getGroupBaseDn() );
        Assert.assertEquals( "cn", dto.getGroupIdAttribute() );
        Assert.assertEquals( "uniqueMember", dto.getGroupMemberAttribute() );
        Assert.assertEquals( "${username}", dto.getGroupMemberFormat() );
        Assert.assertEquals( "groupOfUniqueNames", dto.getGroupObjectClass() );
        Assert.assertEquals( "ou=people", dto.getUserBaseDn() );
        Assert.assertEquals( "uid", dto.getUserIdAttribute() );
        Assert.assertEquals( "inetOrgPerson", dto.getUserObjectClass() );
        Assert.assertNull( dto.getUserPasswordAttribute() );
        // Assert.assertEquals("userPassword", dto.getUserPasswordAttribute());
        Assert.assertEquals( "cn", dto.getUserRealNameAttribute() );
        Assert.assertEquals( "mail", dto.getEmailAddressAttribute() );
        Assert.assertNull( dto.getUserMemberOfAttribute() );
        Assert.assertTrue( dto.isLdapGroupsAsRoles() );
        Assert.assertFalse( dto.isGroupSubtree() );
        Assert.assertFalse( dto.isUserSubtree() );
    }

    private void validateConfigFile( LdapUserAndGroupConfigurationDTO dto )
        throws Exception
    {
        String configFileName = CONF_HOME.getAbsolutePath() + "/no-conf" + "/ldap.xml";// this.getNexusLdapConfiguration();

        LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();
        FileInputStream fis = new FileInputStream( configFileName );
        Configuration config = reader.read( fis );

        CUserAndGroupAuthConfiguration userGroupConfig = config.getUserAndGroupConfig();

        Assert.assertEquals( dto.getGroupBaseDn(), userGroupConfig.getGroupBaseDn() );
        Assert.assertEquals( dto.getGroupIdAttribute(), userGroupConfig.getGroupIdAttribute() );
        Assert.assertEquals( dto.getGroupMemberAttribute(), userGroupConfig.getGroupMemberAttribute() );
        Assert.assertEquals( dto.getGroupMemberFormat(), userGroupConfig.getGroupMemberFormat() );
        Assert.assertEquals( dto.getGroupObjectClass(), userGroupConfig.getGroupObjectClass() );
        Assert.assertEquals( dto.getUserBaseDn(), userGroupConfig.getUserBaseDn() );
        Assert.assertEquals( dto.getUserIdAttribute(), userGroupConfig.getUserIdAttribute() );
        Assert.assertEquals( dto.getUserObjectClass(), userGroupConfig.getUserObjectClass() );
        Assert.assertEquals( dto.getUserPasswordAttribute(), userGroupConfig.getUserPasswordAttribute() );
        Assert.assertEquals( dto.getUserRealNameAttribute(), userGroupConfig.getUserRealNameAttribute() );
        Assert.assertEquals( dto.getEmailAddressAttribute(), userGroupConfig.getEmailAddressAttribute() );
        Assert.assertEquals( dto.getUserMemberOfAttribute(), userGroupConfig.getUserMemberOfAttribute() );
        Assert.assertEquals( dto.isLdapGroupsAsRoles(), userGroupConfig.isLdapGroupsAsRoles() );
        Assert.assertEquals( dto.isGroupSubtree(), userGroupConfig.isGroupSubtree() );
        Assert.assertEquals( dto.isUserSubtree(), userGroupConfig.isUserSubtree() );

    }

    public void testPutNotConfigured()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapUserAndGroupConfigurationResponse response = new LdapUserAndGroupConfigurationResponse();
        LdapUserAndGroupConfigurationDTO userGroupConf = new LdapUserAndGroupConfigurationDTO();
        response.setData( userGroupConf );
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
        userGroupConf.setGroupSubtree( false );
        userGroupConf.setUserSubtree( true );

        LdapUserAndGroupConfigurationResponse result =
            (LdapUserAndGroupConfigurationResponse) resource.put( null, null, null, response );
        Assert.assertEquals( userGroupConf, result.getData() );

        // now how about that get
        result = (LdapUserAndGroupConfigurationResponse) resource.get( null, null, null, null );
        Assert.assertEquals( userGroupConf, result.getData() );

        this.validateConfigFile( userGroupConf );
    }

    /*
     * (non-Javadoc)
     * @see com.sonatype.nexus.AbstractNexusTestCase#customizeContext(org.codehaus.plexus.context.Context)
     */
    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() + "/no-conf/" );
    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();

        // delete the ldap.xml file
        File confFile = new File( CONF_HOME.getAbsolutePath() + "/no-conf/", "ldap.xml" );
        confFile.delete();
    }

}
