package org.sonatype.nexus.client.model;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class RemoveClassAttributeFromJsonStrings extends TestCase
{

    private XStream xstreamJSON = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
    
    public void testJsonStringWithClassAttribute()
    {
        String text = "{\"data\":{\"id\":\"11c75e9aea2\",\"ruleType\":\"exclusive\",\"groupId\":\"*\",\"pattern\":\".*\",\"repositories\":[{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central\",\"@class\":\"repo-routes-member\"},{\"id\":\"thirdparty\",\"name\":\"3rd party\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/thirdparty\",\"@class\":\"repo-routes-member\"},{\"id\":\"central-m1\",\"name\":\"Central M1 shadow\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central-m1\",\"@class\":\"repo-routes-member\"}]}}";
    
        XStreamRepresentation representation = new XStreamRepresentation( this.xstreamJSON, text, MediaType.APPLICATION_JSON );
        
        RepositoryRouteResourceResponse repoRouteResourceResponse = (RepositoryRouteResourceResponse) representation.getPayload( new RepositoryRouteResourceResponse() );
    
        System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );
        
    }
    
    public void DISABLEDtestJsonStringWithOutClassAttribute()
    {
        String text = "{\"data\":{\"id\":\"11c75e9aea2\",\"ruleType\":\"exclusive\",\"groupId\":\"*\",\"pattern\":\".*\",\"repositories\":[{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central\"},{\"id\":\"thirdparty\",\"name\":\"3rd party\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/thirdparty\"},{\"id\":\"central-m1\",\"name\":\"Central M1 shadow\",\"resourceURI\":\"http://localhost:8081/nexus/service/local/repositories/central-m1\"}]}}";
    
        XStreamRepresentation representation = new XStreamRepresentation( this.xstreamJSON, text, MediaType.APPLICATION_JSON );
        
        RepositoryRouteResourceResponse repoRouteResourceResponse = (RepositoryRouteResourceResponse) representation.getPayload( new RepositoryRouteResourceResponse() );
    
        System.out.println( "repoRouteResourceResponse: "+ repoRouteResourceResponse.getData().getPattern() );
        
    }
    
}
