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
package org.sonatype.security.realms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.mock.MockRealm;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;

public class ExternalRoleMappedTest
    extends AbstractSecurityTestCase
{

    private final String SECURITY_CONFIG_FILE_PATH = getBasedir() + "/target/plexus-home/conf/security.xml";

    @Override
    public void configure( Properties properties )
    {
        properties.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
        super.configure( properties );
    }

    public void testUserHasPermissionFromExternalRole()
        throws Exception
    {
        // delete the security conf first, start clean
        new File( SECURITY_CONFIG_FILE_PATH ).delete();

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        Map<String, String> properties = new HashMap<String, String>();
        properties.put( ApplicationPrivilegeMethodPropertyDescriptor.ID, "read" );
        properties.put( ApplicationPrivilegePermissionPropertyDescriptor.ID, "permissionOne" );

        securitySystem.getAuthorizationManager( "default" ).addPrivilege( new Privilege(
                                                                                         "randomId",
                                                                                         "permissionOne",
                                                                                         "permissionOne",
                                                                                         ApplicationPrivilegeDescriptor.TYPE,
                                                                                         properties, false ) );
        securitySystem.getAuthorizationManager( "default" ).addRole( new Role( "mockrole1", "mockrole1", "mockrole1",
                                                                               "default", false, null,
                                                                               Collections.singleton( "randomId" ) ) );

        // add MockRealm to config
        List<String> realms = new ArrayList<String>();
        realms.add( "Mock" );
        realms.add( XmlAuthorizingRealm.ROLE );
        securitySystem.setRealms( realms );
        securitySystem.start();

        // jcohen has the role mockrole1, there is also xml role with the same ID, which means jcohen automaticly has
        // this xml role

        PrincipalCollection jcohen = new SimplePrincipalCollection( "jcohen", MockRealm.NAME );

        try
        {
            securitySystem.checkPermission( jcohen, "permissionOne:invalid" );
            Assert.fail( "Expected AuthorizationException" );
        }
        catch ( AuthorizationException e )
        {
            // expected
        }

        securitySystem.checkPermission( jcohen, "permissionOne:read" ); // throws on error, so this is all we need to do

    }
}
