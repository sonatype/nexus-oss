/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
