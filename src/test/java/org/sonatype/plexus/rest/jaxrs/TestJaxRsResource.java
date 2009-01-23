package org.sonatype.plexus.rest.jaxrs;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.plexus.rest.jsr311.JsrComponent;

@Component( role = JsrComponent.class, hint = "test" )
@Path( "/test" )
public class TestJaxRsResource
    extends AbstractLogEnabled
{
    @GET
    @Produces( { "text/xml", "application/json" } )
    public TestDto get( String param )
    {
        getLogger().info( "Got GET request with param '" + param + "'" );

        TestDto result = new TestDto();

        result.setAString( param );

        result.setADate( new Date() );

        result.getAStringList().add( param );

        result.getAStringList().add( param );

        TestDto child = new TestDto();

        child.setAString( "child" );

        result.getChildren().add( child );

        return result;
    }

    @PUT
    @Consumes( { "application/xml", "application/json" } )
    @Produces( { "application/xml", "application/json" } )
    public String put( TestDto t )
    {
        getLogger().info( "Got TestDTO " + t.getAString() );

        return "OK";
    }
}
