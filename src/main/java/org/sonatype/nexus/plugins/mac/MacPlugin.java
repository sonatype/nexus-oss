package org.sonatype.nexus.plugins.mac;

import java.io.IOException;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.sonatype.nexus.index.context.IndexingContext;

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

    /**
     * Returns the archetype catalog as XML String for given request and sourced from given indexing context. Warning:
     * this may be memory hungry operation if catalog is big! Try to avoid this method use (it is used in CLI and should
     * stay like that).
     * 
     * @param request
     * @param ctx
     * @return
     * @throws IOException
     */
    String listArchetypesAsCatalogXML( MacRequest request, IndexingContext ctx )
        throws IOException;
}
