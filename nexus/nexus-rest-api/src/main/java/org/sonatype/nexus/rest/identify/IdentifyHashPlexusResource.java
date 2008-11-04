package org.sonatype.nexus.rest.identify;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Resource that is able to fetch the identified Nexus Artifact. The used hash algorithm and hash key are coming from
 * request attributes, and are posibly mapped from URL. Recognized algorithms: "sha1" and "md5".
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "IdentifyHashPlexusResource" )
public class IdentifyHashPlexusResource
    extends AbstractNexusPlexusResource
{

    public static final String ALGORITHM_KEY = "algorithm";

    public static final String HASH_KEY = "hash";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/identify/{" + ALGORITHM_KEY + "}/{" + HASH_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/identify/*/*", "authcBasic,perms[nexus:identify]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String alg = request.getAttributes().get( ALGORITHM_KEY ).toString();

        String checksum = request.getAttributes().get( HASH_KEY ).toString();

        NexusArtifact na = null;

        try
        {
            if ( "md5".equalsIgnoreCase( alg ) )
            {
                na = ai2Na( request, getNexus().identifyArtifact( ArtifactInfo.MD5, checksum ) );
            }
            else if ( "sha1".equalsIgnoreCase( alg ) )
            {
                na = ai2Na( request, getNexus().identifyArtifact( ArtifactInfo.SHA1, checksum ) );
            }
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }

        return na;
    }

}
