package org.sonatype.nexus.integrationtests.nexus3699;

import java.io.IOException;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.test.utils.CapabilitiesMessageUtil;

public class Nexus3699CapabilityIT
    extends AbstractNexusIntegrationTest
{

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "capabilities.xml", WORK_CONF_DIR );
    }

    @Test
    public void list()
        throws Exception
    {
        List<CapabilityListItemResource> data = CapabilitiesMessageUtil.list();

        Assert.assertFalse( data.isEmpty() );
        Assert.assertThat( data.get( 0 ).getId(), CoreMatchers.equalTo( "4fde59a80f4" ) );
        Assert.assertThat( data.get( 0 ).getName(), CoreMatchers.equalTo( "test-capability" ) );
        Assert.assertThat( data.get( 0 ).getTypeId(), CoreMatchers.equalTo( "TouchTest" ) );
    }

    @Test
    public void crud()
    {

    }

}
