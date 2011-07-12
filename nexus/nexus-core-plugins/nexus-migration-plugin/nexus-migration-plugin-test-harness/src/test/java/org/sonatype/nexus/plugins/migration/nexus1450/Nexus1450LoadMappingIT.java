package org.sonatype.nexus.plugins.migration.nexus1450;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1450LoadMappingIT
    extends AbstractMigrationIntegrationTest
{

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "mapping.xml", WORK_CONF_DIR );
    }

    @Test
    public void loadMap()
        throws Exception
    {
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/artifactory-repo/nexus1450/artifact/1.0/artifact-1.0.jar" );

        Status status = RequestFacade.sendMessage( url, Method.GET, null ).getStatus();
        Assert.assertTrue( "Unable to download artifact " + status + " " + url, status.isSuccess() );
    }

}
