/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
