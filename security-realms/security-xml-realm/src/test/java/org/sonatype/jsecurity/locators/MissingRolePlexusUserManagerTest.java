package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;

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
