package org.sonatype.nexus.integrationtests.nexus1230;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Nexus1230ImportArtifactory125Test
    extends AbstractNexusIntegrationTest
{

    @Test
    public void importArtifactory125()
        throws Exception
    {
        ImportMessageUtil.importBackup( getTestFile( "artifactory125.zip" ) );
    }
}
