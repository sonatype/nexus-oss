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
package org.sonatype.nexus.rest;

import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeNode;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifactHit;
import org.sonatype.nexus.rest.model.NexusNGArtifactLink;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

public class XStreamInitializer
{
    public static void init( XStream xstream )
    {
        xstream.processAnnotations( SearchResponse.class );

        xstream.registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class,
            "artifact" ) );

        // Tree
        xstream.processAnnotations( IndexBrowserTreeViewResponseDTO.class );
        xstream.processAnnotations( IndexBrowserTreeNode.class );
        xstream.registerLocalConverter( IndexBrowserTreeNode.class, "children", new AliasingListConverter(
            IndexBrowserTreeNode.class, "child" ) );

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
    }
}
