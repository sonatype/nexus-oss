package org.sonatype.nexus.integrationtests.nexus2996;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.plugin.XStreamFactory;

public class Nexus2996DeleteRepoTargetTest
    extends AbstractNexusIntegrationTest
{

    private PrivilegesMessageUtil privUtil =
        new PrivilegesMessageUtil( XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );

    private static final String TARGET_ID = "1c1fd83a2fd9";

    private static final String READ_PRIV_ID = "1c26537599f6";

    private static final String CREATE_PRIV_ID = "1c2652734258";

    private static final String UPDATE_PRIV_ID = "1c2653b9a119";

    private static final String DELETE_PRIV_ID = "1c2653f5a3e2";

    @Test
    public void deleteRepoTarget()
        throws Exception
    {
        RepositoryTargetResource target = TargetMessageUtil.get( TARGET_ID );
        Assert.assertThat( target.getPatterns(), IsCollectionContaining.hasItem( ".*" ) );

        privUtil.assertExists( READ_PRIV_ID, CREATE_PRIV_ID, UPDATE_PRIV_ID, DELETE_PRIV_ID );

        Assert.assertTrue( TargetMessageUtil.delete( TARGET_ID ).getStatus().isSuccess() );

        privUtil.assertNotExists( READ_PRIV_ID, CREATE_PRIV_ID, UPDATE_PRIV_ID, DELETE_PRIV_ID );
    }
}
