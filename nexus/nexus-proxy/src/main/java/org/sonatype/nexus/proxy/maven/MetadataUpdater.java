package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.Collection;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;

/**
 * A metadata updater that offers simple metadata management services.
 * 
 * @author cstamas
 */
public interface MetadataUpdater
{
    //
    // "Single shot" methods, used from Nexus to maintain metadata on-the-fly
    //

    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void deployArtifact( ArtifactStoreRequest request, MetadataLocator locator )
        throws IOException;

    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void undeployArtifact( ArtifactStoreRequest request, MetadataLocator locator )
        throws IOException;

    //
    // "Multi shot" methods, used from Nexus/CLI tools to maintain metadata in batch/scanning mode
    //

    /**
     * Calling this method <b>replaces</b> the GAV, GA and G metadatas accordingly.
     * 
     * @param req
     */
    void deployArtifacts( Collection<ArtifactStoreRequest> requests, MetadataLocator locator )
        throws IOException;

    /**
     * Give me a coll, and i will createate the metadata.
     * 
     * @param coll
     * @param locator
     * @throws IOException
     */
    void recreateMetadata( StorageCollectionItem coll, MetadataLocator locator )
        throws IOException;
}
