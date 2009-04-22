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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserManager;

public class MissingRolePlexusUserManagerTest
    extends PlexusTestCase
{

    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir()
        + "/target/test-classes/org/sonatype/jsecurity/locators/missingRoleTest-security.xml";

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }

    // private Set<String> getXMLRoles() throws Exception
    // {
    // PlexusRoleLocator locator = (PlexusRoleLocator) this.lookup( PlexusRoleLocator.class );
    // return locator.listRoleIds();
    // }

    private PlexusUserManager getUserManager()
        throws Exception
    {
        return (PlexusUserManager) this.lookup( PlexusUserManager.class, "additinalRoles" );
    }

    public void testInvalidRoleMapping()
        throws Exception
    {
        PlexusUserManager userManager = this.getUserManager();

        PlexusUser user = userManager.getUser( "jcoder" );
        Assert.assertNotNull( user );

        Set<String> roleIds = new HashSet<String>();
        for ( PlexusRole role : user.getRoles() )
        {
            Assert.assertNotNull( "User has null role.", role );
            roleIds.add( role.getRoleId() );
        }
        Assert.assertFalse( roleIds.contains( "INVALID-ROLE-BLA-BLA" ) );
    }

}
