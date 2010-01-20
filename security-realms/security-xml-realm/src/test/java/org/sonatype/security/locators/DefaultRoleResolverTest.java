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
package org.sonatype.security.locators;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.security.authorization.xml.RoleResolver;

public class DefaultRoleResolverTest
    extends PlexusTestCase
{

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put(
            "security-xml-file",
            "target/test-classes/org/sonatype/security/locators/DefaultRoleResolverTest-security.xml" );
    }

    public void testResolveRoles()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role1" );
        roleIds.add( "role3" );

        Set<String> result = roleResolver.resolveRoles( roleIds );

        Assert.assertTrue( result.contains( "role1" ) );
        Assert.assertTrue( result.contains( "role2" ) );
        Assert.assertTrue( result.contains( "role3" ) );
        Assert.assertTrue( result.contains( "role4" ) );
        Assert.assertTrue( result.contains( "role5" ) );

        Assert.assertEquals( 5, result.size() );

    }

    public void testResolveRolesSingleRole()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role5" );

        Set<String> result = roleResolver.resolveRoles( roleIds );

        Assert.assertTrue( result.contains( "role5" ) );

        Assert.assertEquals( 1, result.size() );

    }

    public void testResolveRolesInvalidRole()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "INVALID-ROLE-asdfalksjdf" );

        Set<String> result = roleResolver.resolveRoles( roleIds );

        Assert.assertEquals( 0, result.size() );

    }

    public void testResolveRolesEmpty()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "" );

        Set<String> result = roleResolver.resolveRoles( roleIds );

        Assert.assertEquals( 0, result.size() );

        // nothing
        roleIds.clear();

        result = roleResolver.resolveRoles( roleIds );

        Assert.assertEquals( 0, result.size() );

        // null
        roleIds.clear();
        roleIds.add( null );

        result = roleResolver.resolveRoles( roleIds );

        Assert.assertEquals( 0, result.size() );
    }

    /*
     * PERMISSIONS
     */

    public void testResolvePermissions()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role3" );
        // contains role3, 4, 5

        Set<String> result = roleResolver.resolvePermissions( roleIds );

        Assert.assertTrue( "permssions: " + result, result.contains( "priv1-ONE:read" ) );
        Assert.assertTrue( result.contains( "priv2-TWO:read" ) );
        Assert.assertTrue( result.contains( "priv3-THREE:read" ) );
        Assert.assertTrue( result.contains( "priv4-FOUR:read" ) );
        Assert.assertTrue( result.contains( "priv5-FIVE:read" ) );

        // these are from the original security.xml
        Assert.assertTrue( result.contains( "/some/path1/:read" ) );
        Assert.assertTrue( result.contains( "/some/path4/:read" ) );

        Assert.assertEquals( "permssions: " + result, 7, result.size() );

    }

    public void testResolvePermissionsSingleRole()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "role5" );

        Set<String> result = roleResolver.resolvePermissions( roleIds );

        Assert.assertTrue( result.contains( "priv3-THREE:read" ) );
        Assert.assertTrue( result.contains( "priv4-FOUR:read" ) );
        Assert.assertTrue( result.contains( "priv5-FIVE:read" ) );

        Assert.assertEquals( 3, result.size() );

    }

    public void testResolvePermissionsInvalidRole()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "INVALID-ROLE-asdfalksjdf" );

        Set<String> result = roleResolver.resolvePermissions( roleIds );

        Assert.assertEquals( 0, result.size() );

    }

    public void testResolvePermissionsEmpty()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );

        Set<String> roleIds = new HashSet<String>();
        roleIds.add( "" );

        Set<String> result = roleResolver.resolvePermissions( roleIds );

        Assert.assertEquals( 0, result.size() );

        // nothing
        roleIds.clear();

        result = roleResolver.resolvePermissions( roleIds );

        Assert.assertEquals( 0, result.size() );

        // null
        roleIds.clear();
        roleIds.add( null );

        result = roleResolver.resolvePermissions( roleIds );

        Assert.assertEquals( 0, result.size() );
    }
    
    public void testEffectiveRolesNested()
        throws Exception
    {
        RoleResolver roleResolver = this.lookup( RoleResolver.class );
        
        Set<String> effectiveRoles = roleResolver.effectiveRoles( Collections.singleton( "role10" ) );
        
        Assert.assertEquals( 4, effectiveRoles.size() );
        
        Assert.assertTrue( effectiveRoles.contains( "role10" ) );
        Assert.assertTrue( effectiveRoles.contains( "role11" ) );
        Assert.assertTrue( effectiveRoles.contains( "role12" ) );
        Assert.assertTrue( effectiveRoles.contains( "role13" ) );
        
        effectiveRoles = roleResolver.effectiveRoles( Collections.singleton( "role11" ) );
        
        Assert.assertEquals( 3, effectiveRoles.size() );
        
        Assert.assertTrue( effectiveRoles.contains( "role11" ) );
        Assert.assertTrue( effectiveRoles.contains( "role12" ) );
        Assert.assertTrue( effectiveRoles.contains( "role13" ) );
        
        effectiveRoles = roleResolver.effectiveRoles( Collections.singleton( "role12" ) );
        
        Assert.assertEquals( 2, effectiveRoles.size() );
        
        Assert.assertTrue( effectiveRoles.contains( "role12" ) );
        Assert.assertTrue( effectiveRoles.contains( "role13" ) );
        
        effectiveRoles = roleResolver.effectiveRoles( Collections.singleton( "role13" ) );
        
        Assert.assertEquals( 1, effectiveRoles.size() );
        
        Assert.assertTrue( effectiveRoles.contains( "role13" ) );
    }

}
