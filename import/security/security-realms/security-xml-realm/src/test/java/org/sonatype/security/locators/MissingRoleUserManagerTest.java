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
package org.sonatype.security.locators;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;

public class MissingRoleUserManagerTest
    extends AbstractSecurityTestCase
{

    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    private final String SECURITY_CONFIG_FILE_PATH = getBasedir()
        + "/target/test-classes/org/sonatype/jsecurity/locators/missingRoleTest-security.xml";

    @Override
    public void configure( Properties properties )
    {
        properties.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
        super.configure( properties );
    }

    // private Set<String> getXMLRoles() throws Exception
    // {
    // PlexusRoleLocator locator = (PlexusRoleLocator) this.lookup( PlexusRoleLocator.class );
    // return locator.listRoleIds();
    // }

    private SecuritySystem getSecuritySystem()
        throws Exception
    {
        return (SecuritySystem) this.lookup( SecuritySystem.class );
    }

    public void testInvalidRoleMapping()
        throws Exception
    {
        SecuritySystem userManager = this.getSecuritySystem();

        User user = userManager.getUser( "jcoder" );
        Assert.assertNotNull( user );

        Set<String> roleIds = new HashSet<String>();
        for ( RoleIdentifier role : user.getRoles() )
        {
            Assert.assertNotNull( "User has null role.", role );
            roleIds.add( role.getRoleId() );
        }
        Assert.assertFalse( roleIds.contains( "INVALID-ROLE-BLA-BLA" ) );
    }

}
