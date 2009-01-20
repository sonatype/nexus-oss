/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.client.model;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class RemoveClassAttributeFromJsonStrings
    extends TestCase
{
    private XStream xstreamJSON;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        NexusApplication napp = new NexusApplication();

        xstreamJSON = napp.doConfigureXstream( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
    }

    public void testJsonStringWithClassAttribute()
    {
        String text = "{\"data\":{\"id\":\"11c75e9aea2\",\"ruleType\":\"exclusive\",\"groupId\":\"*\",\"pattern\":\".*\",\"repositories\":[{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central\",\"@class\":\"repo-routes-member\"},{\"id\":\"thirdparty\",\"name\":\"3rd party\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/thirdparty\",\"@class\":\"repo-routes-member\"},{\"id\":\"central-m1\",\"name\":\"Central M1 shadow\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central-m1\",\"@class\":\"repo-routes-member\"}]}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            this.xstreamJSON,
            text,
            MediaType.APPLICATION_JSON );

        RepositoryRouteResourceResponse repoRouteResourceResponse = (RepositoryRouteResourceResponse) representation
            .getPayload( new RepositoryRouteResourceResponse() );

        // System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );

    }

    public void testJsonStringWithOutClassAttribute()
    {
        String text = "{\"data\":{\"id\":\"11c75e9aea2\",\"ruleType\":\"exclusive\",\"groupId\":\"*\",\"pattern\":\".*\",\"repositories\":[{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central\"},{\"id\":\"thirdparty\",\"name\":\"3rd party\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/thirdparty\"},{\"id\":\"central-m1\",\"name\":\"Central M1 shadow\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central-m1\"}]}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            this.xstreamJSON,
            text,
            MediaType.APPLICATION_JSON );

        RepositoryRouteResourceResponse repoRouteResourceResponse = (RepositoryRouteResourceResponse) representation
            .getPayload( new RepositoryRouteResourceResponse() );

        // System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );

    }

    // public void testScheduleJsonStringWithClassAttribute()
    // {
    // String text =
    // "{\"data\":{\"id\":\"11c75e9aea2\",\"ruleType\":\"exclusive\",\"groupId\":\"*\",\"pattern\":\".*\",\"repositories\":[{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central\",\"@class\":\"repo-routes-member\"},{\"id\":\"thirdparty\",\"name\":\"3rd party\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/thirdparty\",\"@class\":\"repo-routes-member\"},{\"id\":\"central-m1\",\"name\":\"Central M1 shadow\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central-m1\",\"@class\":\"repo-routes-member\"}]}}";
    //    
    // XStreamRepresentation representation = new XStreamRepresentation( this.xstreamJSON, text,
    // MediaType.APPLICATION_JSON );
    //        
    // RepositoryRouteResourceResponse repoRouteResourceResponse = (RepositoryRouteResourceResponse)
    // representation.getPayload( new RepositoryRouteResourceResponse() );
    //    
    // System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );
    //        
    // }

    public void testScheduleJsonStringWithOutClassAttribute()
    {
        String text = "{\"data\":{\"id\":null,\"name\":\"clear repo cache\",\"enabled\":true,\"typeId\":\"ClearCacheTask\",\"schedule\":\"manual\",\"properties\":[{\"id\":\"repositoryOrGroupId\",\"value\":\"all_repo\"},{\"id\":\"resourceStorePath\",\"value\":\"\"}]}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            this.xstreamJSON,
            text,
            MediaType.APPLICATION_JSON );

        ScheduledServiceResourceResponse repoRouteResourceResponse = (ScheduledServiceResourceResponse) representation
            .getPayload( new ScheduledServiceResourceResponse() );

        // System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );

    }

}
