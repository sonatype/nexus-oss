/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.security.locators.MockStaticSecurityResource;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;

import com.google.inject.Binder;
import com.google.inject.name.Names;

public class ResourceMergingConfigurationManagerTest
    extends InjectedTestCase
{
    private ConfigurationManager manager;

    @Override
    public void configure( Properties properties )
    {
        super.configure( properties );
        properties.put( "security-xml-file",
                        "target/test-classes/org/sonatype/security/configuration/static-merging/security.xml" );
    }

    @Override
    public void configure( Binder binder )
    {
        super.configure( binder );
        binder.bind( StaticSecurityResource.class ).annotatedWith( Names.named( "UnitTestSecurityResource" ) ).to( UnitTestSecurityResource.class );
        binder.bind( StaticSecurityResource.class ).annotatedWith( Names.named( "mock" ) ).to( MockStaticSecurityResource.class );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        manager = lookup( ConfigurationManager.class, "resourceMerging" );
    }

    public void testRoleMerging()
        throws Exception
    {
        List<CRole> roles = manager.listRoles();

        CRole anon = manager.readRole( "anon" );
        assertTrue( "roles: " + anon.getRoles(), anon.getRoles().contains( "other" ) );
        assertTrue( "roles: " + anon.getRoles(), anon.getRoles().contains( "role2" ) );
        assertEquals( "roles: " + anon.getRoles(), 2, anon.getRoles().size() );

        assertTrue( anon.getPrivileges().contains( "priv1" ) );
        assertTrue( anon.getPrivileges().contains( "4-test" ) );
        assertEquals( "privs: " + anon.getPrivileges(), 2, anon.getPrivileges().size() );

        assertEquals( "Test Anon Role", anon.getName() );
        assertEquals( "Test Anon Role Description", anon.getDescription() );
        assertEquals( 60, anon.getSessionTimeout() );

        CRole other = manager.readRole( "other" );
        assertTrue( other.getRoles().contains( "role2" ) );
        assertEquals( "roles: " + other.getRoles(), 1, other.getRoles().size() );

        assertTrue( other.getPrivileges().contains( "6-test" ) );
        assertTrue( other.getPrivileges().contains( "priv2" ) );
        assertEquals( "privs: " + other.getPrivileges(), 2, other.getPrivileges().size() );

        assertEquals( "Other Role", other.getName() );
        assertEquals( "Other Role Description", other.getDescription() );
        assertEquals( 60, other.getSessionTimeout() );

        // all roles
        assertEquals( 8, roles.size() );

    }

    public void testPrivsMerging()
        throws Exception
    {
        List<CPrivilege> privs = manager.listPrivileges();

        CPrivilege priv = manager.readPrivilege( "1-test" );
        assertTrue( priv != null );

        priv = manager.readPrivilege( "2-test" );
        assertTrue( priv != null );

        priv = manager.readPrivilege( "4-test" );
        assertTrue( priv != null );

        priv = manager.readPrivilege( "5-test" );
        assertTrue( priv != null );

        priv = manager.readPrivilege( "6-test" );
        assertTrue( priv != null );

        assertNotNull( manager.readPrivilege( "priv1" ) );
        assertNotNull( manager.readPrivilege( "priv2" ) );
        assertNotNull( manager.readPrivilege( "priv3" ) );
        assertNotNull( manager.readPrivilege( "priv4" ) );
        assertNotNull( manager.readPrivilege( "priv5" ) );

        assertEquals( "privs: " + this.privilegeListToStringList( privs ), 10, privs.size() );
    }

    private List<String> privilegeListToStringList( List<CPrivilege> privs )
    {
        List<String> ids = new ArrayList<String>();

        for ( CPrivilege priv : privs )
        {
            ids.add( priv.getId() );
        }

        return ids;
    }
}
