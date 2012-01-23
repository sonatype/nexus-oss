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
package org.sonatype.nexus.plugins.mac;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.item.ContentLocator;

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

    private volatile String payload;

    public ArchetypeContentLocator( String repositoryId, IndexingContext indexingContext, MacPlugin macPlugin,
                                    ArtifactInfoFilter artifactInfoFilter )
    {
        this.repositoryId = repositoryId;
        this.indexingContext = indexingContext;
        this.macPlugin = macPlugin;
        this.artifactInfoFilter = artifactInfoFilter;
    }

    protected synchronized String generateCatalogPayload()
        throws IOException
    {
        if ( payload == null )
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

            payload = sw.toString();
        }

        return payload;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        return new ByteArrayInputStream( generateCatalogPayload().getBytes( "UTF-8" ) );
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
