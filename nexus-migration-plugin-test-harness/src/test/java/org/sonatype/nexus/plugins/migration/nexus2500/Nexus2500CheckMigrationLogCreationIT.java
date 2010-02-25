package org.sonatype.nexus.plugins.migration.nexus2500;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrator;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;

public class Nexus2500CheckMigrationLogCreationIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void checkAvailableLogs()
        throws Exception
    {
        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( "Status: \n" + responseText, 200, response.getStatus().getCode() );

        LogsListResourceResponse logListResponse =
            (LogsListResourceResponse) this.getXMLXStream().fromXML( responseText );
        List<LogsListResource> logList = logListResponse.getData();
        Assert.assertTrue( "Log List should contain at least 1 log.", logList.size() > 0 );

        List<String> names = new ArrayList<String>();
        for ( Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); )
        {
            LogsListResource logResource = iter.next();

            names.add( logResource.getName() );
        }

        Assert.assertTrue( names.contains( ArtifactoryMigrator.MIGRATION_LOG ) );
    }

}
