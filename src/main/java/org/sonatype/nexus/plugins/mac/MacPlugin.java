package org.sonatype.nexus.plugins.mac;

import java.io.IOException;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.index.context.IndexingContext;

/**
 * The MavenArchetypePlugin's main component.
 * 
 * @author cstamas
 */
public interface MacPlugin
{
    /**
     * Returns the archetype catalog for given request and sourced from given indexing context.
     * 
     * @param request
     * @param ctx
     * @return
     * @throws IOException
     */
    ArchetypeCatalog listArcherypesAsCatalog( MacRequest request, IndexingContext ctx )
        throws IOException;
}
