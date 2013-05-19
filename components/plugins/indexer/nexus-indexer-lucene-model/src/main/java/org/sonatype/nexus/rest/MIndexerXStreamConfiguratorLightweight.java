/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import org.sonatype.nexus.rest.model.AliasingListConverter;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifactHit;
import org.sonatype.nexus.rest.model.NexusNGArtifactLink;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;

import com.thoughtworks.xstream.XStream;

/**
 * The "lightweight" XStream configurator for Nexus Indexer Lucene plugin. It will configure XStream for all DTOs except
 * the "tree view" ones (thus, tree view is unusable in case if "lightweight" clients, but it's the least, that's not
 * what clients would like to use). Problem with "tree view" is that it uses deep class hierarchy going all way back to
 * Maven Indexer classes, and hence, on client side all that entourage would need to be present on classpath, that would
 * defy the use of REST API at all (if indexer is present locally, then just download indexes and perform searches
 * locally too).
 * 
 * @author cstamas
 * @since 2.1
 */
public class MIndexerXStreamConfiguratorLightweight
{
    public static XStream configureXStream( final XStream xstream )
    {
        xstream.processAnnotations( SearchResponse.class );
        xstream.registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class,
            "artifact" ) );

        // NG
        xstream.processAnnotations( SearchNGResponse.class );
        xstream.processAnnotations( NexusNGArtifact.class );
        xstream.processAnnotations( NexusNGArtifactHit.class );
        xstream.processAnnotations( NexusNGArtifactLink.class );
        xstream.registerLocalConverter( SearchNGResponse.class, "data", new AliasingListConverter(
            NexusNGArtifact.class, "artifact" ) );
        xstream.registerLocalConverter( NexusNGArtifact.class, "artifactHits", new AliasingListConverter(
            NexusNGArtifactHit.class, "artifactHit" ) );
        xstream.registerLocalConverter( NexusNGArtifactHit.class, "artifactLinks", new AliasingListConverter(
            NexusNGArtifactLink.class, "artifactLink" ) );
        return xstream;
    }
}
