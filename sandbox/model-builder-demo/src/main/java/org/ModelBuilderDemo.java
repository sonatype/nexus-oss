/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
