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

import java.net.URL;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.UrlModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

public class SimpleModelResolver
    implements ModelResolver
{

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        // no-op, we don't care about POM repos right now
    }

    public ModelResolver newCopy()
    {
        // this class is stateless so can be reused
        return this;
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        // do your repo magic here

        String resource = "/repo/" + groupId + '/' + artifactId + '/' + version + "/pom.xml";

        URL url = getClass().getResource( resource );

        if ( url == null )
        {
            throw new UnresolvableModelException( "PANIC!", groupId, artifactId, version );
        }

        return new UrlModelSource( url );
    }

}
