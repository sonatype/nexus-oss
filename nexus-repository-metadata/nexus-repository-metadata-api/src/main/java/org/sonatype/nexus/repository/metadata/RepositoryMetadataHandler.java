package org.sonatype.nexus.repository.metadata;

import java.io.IOException;

import org.sonatype.nexus.repository.metadata.model.OrderedRepositoryMirrorsMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

/**
 * The repository metadata handler interface. Defines the basic operations for retrieving and storing the metadata, and
 * retrieving the dynamic list of mirros.
 * 
 * @author cstamas
 */
public interface RepositoryMetadataHandler
{
    /**
     * Fetches the metadata. Returns null if metadata is not found. In case of transport or other IO problem,
     * IOException is raised.
     * 
     * @param request
     * @return the metadata or null if not found.
     * @throws IOException
     */
    RepositoryMetadata readRepositoryMetadata( MetadataRequest request )
        throws IOException;

    /**
     * Stores the metadata. In case of transport or other IO problem, IOException is raised.
     * 
     * @param request
     * @param metadata
     * @throws IOException
     */
    void writeRepositoryMetadata( MetadataRequest request, RepositoryMetadata metadata )
        throws IOException;

    /**
     * Returns the ordered list of mirrors for given metadata. What (or from where) is returned depends on metadata: if
     * mirrorListSource field is present, it will try to fetch it from there. If not, it will fallback to "local" GeoIP
     * matching, if possible. As last resort, it will return the mirrors field of metadata in unmodified form.
     * <p>
     * The "strategy" fields tells how the result should be interpreted:
     * <ul>
     * <li>SERVER - the list should be consumed as is, since server strategy already formed the "best" mirror for us.</li>
     * <li>CLIENT_AUTO - if it is possible, local side GeoIP matchind is applied to the list, so it should be consumed
     * as-is.</li>
     * <li>CLIENT_MANUAL - the list is 1:1 copy of the "static" mirror list without any reordering. In this scenario,
     * the client of this API would have to offer the list to the user over some sort of UI to introspection.</li>
     * </ul>
     * 
     * @param url
     * @param transport
     * @return
     * @throws IOException
     */
    OrderedRepositoryMirrorsMetadata fetchOrderedMirrorMetadata( RepositoryMetadata metadata, RawTransport transport )
        throws IOException;
}
