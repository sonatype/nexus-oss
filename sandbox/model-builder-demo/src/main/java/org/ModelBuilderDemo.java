package org;

import java.io.StringWriter;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.UrlModelSource;
import org.apache.maven.model.io.ModelWriter;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;

public class ModelBuilderDemo
{

    public static void main( String[] args )
        throws Exception
    {
        /*
         * setup the container and get us some interesting components
         */

        PlexusContainer container = new DefaultPlexusContainer();

        ModelBuilder modelBuilder = container.lookup( ModelBuilder.class );

        ModelWriter modelWriter = container.lookup( ModelWriter.class );

        /*
         * build some effective model
         */

        ModelSource modelSource =
            new UrlModelSource( ModelBuilderDemo.class.getResource( "/repo/demo/dependency/1.0/pom.xml" ) );

        ModelBuildingRequest request = new DefaultModelBuildingRequest();

        request.setModelSource( modelSource );
        request.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
        request.setSystemProperties( System.getProperties() );
        request.setModelResolver( new SimpleModelResolver() );

        ModelBuildingResult result = modelBuilder.build( request );

        Model effectivePom = result.getEffectiveModel();

        StringWriter writer = new StringWriter();
        modelWriter.write( writer, null, effectivePom );
        System.out.println( writer.toString() );
    }

}
