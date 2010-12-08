package org.sonatype.nexus.integrationtests.nexus3936;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.test.utils.NexusIllegalStateException;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.security.rest.model.ClientPermission;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Security is already disabled for this Test, we just need to make sure the Status resource returns ALL/15, for all the
 * permission strings.
 */
@Test( groups = { "security", "anonymous", "status" } )
public class Nexus3936DisableSecurityStatusIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testSecurityDisabledStatus()
        throws NexusIllegalStateException
    {

        NexusStatusUtil statusUtil = new NexusStatusUtil();
        StatusResource statusResource = statusUtil.getNexusStatus().getData();

        List<ClientPermission> permisisons = statusResource.getClientPermissions().getPermissions();

        Assert.assertTrue( permisisons.size() > 0, "Permissions are empty, expected a whole bunch, not zero." );
        for ( ClientPermission clientPermission : permisisons )
        {
            Assert.assertEquals( clientPermission.getValue(), 15, "Permission '"+ clientPermission.getId() +"' should have had a value of '15', the value was" + clientPermission.getValue() );
        }
        // that is it, just checking the values, when security is disabled, access is WIDE open.
    }
}
