/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.tools;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;

public class UserRoleMappingTest
    extends PlexusTestCase
{

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setClassPathScanning( true );
    }
    
    public ConfigurationManager getConfigManager()
        throws Exception
    {
        return (ConfigurationManager) this.lookup( ConfigurationManager.class );
    }

    public void testGetUser()
        throws Exception
    {
        ConfigurationManager config = this.getConfigManager();

        CUser user = config.readUser( "test-user" );
        Assert.assertEquals( user.getId(), "test-user" );
        Assert.assertEquals( user.getEmail(), "changeme1@yourcompany.com" );
        Assert.assertEquals( user.getFirstName(), "Test" );
        Assert.assertEquals( user.getLastName(), "User" );
        Assert.assertEquals( user.getPassword(), "b2a0e378437817cebdf753d7dff3dd75483af9e0" );
        Assert.assertEquals( user.getStatus(), "active" );

        CUserRoleMapping mapping = config.readUserRoleMapping( "test-user", "default" );

        Assert.assertTrue( mapping.getRoles().contains( "role1" ) );
        Assert.assertTrue( mapping.getRoles().contains( "role2" ) );
        Assert.assertEquals( 2, mapping.getRoles().size() );
    }

    public void testUpdateUsersRoles()
        throws Exception
    {
        ConfigurationManager config = this.getConfigManager();

        // make sure we have exactly 4 user role mappings
        Assert.assertEquals( 4, config.listUserRoleMappings().size() );

        // get the test-user and add a role
        CUser user = config.readUser( "test-user" );

        CUserRoleMapping roleMapping = config.readUserRoleMapping( "test-user", "default" );
        List<String> roles = roleMapping.getRoles();
        roles.add( "role3" );

        // update the user
        config.updateUser( user, new HashSet<String>( roles ) );

        // make sure we have exactly 4 user role mappings
        Assert.assertEquals( 4, config.listUserRoleMappings().size() );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy the file to a different location because we are going to change it
        FileUtils.copyFile( new File( "target/test-classes/org/sonatype/security/locators/security.xml" ),
                            new File( "target/test-classes/org/sonatype/security/locators/security-test.xml" ) );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "security-xml-file", "target/test-classes/org/sonatype/security/locators/security-test.xml" );
    }

}
