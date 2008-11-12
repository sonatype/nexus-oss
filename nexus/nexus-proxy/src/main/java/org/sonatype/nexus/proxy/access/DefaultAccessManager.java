package org.sonatype.nexus.proxy.access;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A default access manager relying onto default NexusAuthorizer.
 * 
 * @author cstamas
 */
@Component( role = AccessManager.class )
public class DefaultAccessManager
    implements AccessManager
{
    @Requirement
    private NexusItemAuthorizer nexusItemAuthorizer;

    public void decide( ResourceStoreRequest request, Repository repository, Action action )
        throws AccessDeniedException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        if ( !nexusItemAuthorizer.authorizePath( uid, request.getRequestContext(), action ) )
        {
            // deny the access
            throw new AccessDeniedException( request, "Access denied on repository ID='" + repository.getId()
                + "', path='" + request.getRequestPath() + "', action='" + action + "'!" );
        }
    }
}
