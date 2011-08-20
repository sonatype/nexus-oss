/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.migration.nexus2500;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrator;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.testng.annotations.Test;

public class Nexus2500CheckMigrationLogCreationIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void checkAvailableLogs()
        throws Exception
    {
        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        assertThat( "Status: \n" + responseText, response.getStatus().getCode(), is( equalTo( 200 ) ) );

        LogsListResourceResponse logListResponse =
            (LogsListResourceResponse) this.getXMLXStream().fromXML( responseText );
        List<LogsListResource> logList = logListResponse.getData();
        assertThat( "Log List should contain at least 1 log.", logList.size() > 0 );

        List<String> names = new ArrayList<String>();
        for ( Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); )
        {
            LogsListResource logResource = iter.next();

            names.add( logResource.getName() );
        }

        assertThat( names, hasItem( ArtifactoryMigrator.MIGRATION_LOG ) );
    }

}
