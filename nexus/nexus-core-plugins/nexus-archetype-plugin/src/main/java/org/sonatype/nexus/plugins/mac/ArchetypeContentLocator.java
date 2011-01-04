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
package org.sonatype.nexus.plugins.mac;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StringContentLocator;

/**
 * A content locator to generate archetype catalog. This way, the actual work (search, archetype catalog model fillup
 * from results, converting it to string and flushing it as byte array backed stream) is postponed to very last moment,
 * when the content itself is asked for.
 * 
 * @author cstamas
 */
public class ArchetypeContentLocator
    implements ContentLocator
{
    private final String repositoryId;

    private final IndexingContext indexingContext;

    private final MacPlugin macPlugin;

    private final ArtifactInfoFilter artifactInfoFilter;

    public ArchetypeContentLocator( String repositoryId, IndexingContext indexingContext, MacPlugin macPlugin,
                                    ArtifactInfoFilter artifactInfoFilter )
    {
        this.repositoryId = repositoryId;
        this.indexingContext = indexingContext;
        this.macPlugin = macPlugin;
        this.artifactInfoFilter = artifactInfoFilter;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        // TODO: what if URL is needed?
        // this content generator will be sucked from the repo root,
        // so it is fine for it to have no repositoryUrl
        // perm filter added, now this generator will generate catalog with archetypes that user
        // fetching it may see

        // TODO: we have now the URL too, but I want to wait for ArchetypeCatalog improvements and possible changes
        MacRequest req = new MacRequest( repositoryId, null, artifactInfoFilter );

        // get the catalog
        ArchetypeCatalog catalog = macPlugin.listArcherypesAsCatalog( req, indexingContext );

        // serialize it to XML
        StringWriter sw = new StringWriter();

        ArchetypeCatalogXpp3Writer writer = new ArchetypeCatalogXpp3Writer();

        writer.write( sw, catalog );

        return new StringContentLocator( sw.toString() ).getContent();
    }

    @Override
    public String getMimeType()
    {
        return "text/xml";
    }

    @Override
    public boolean isReusable()
    {
        return true;
    }
}
