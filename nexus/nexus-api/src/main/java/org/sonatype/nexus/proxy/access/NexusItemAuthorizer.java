package org.sonatype.nexus.proxy.access;

import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Authorizes the Repository requests against permissions.
 * 
 * @author cstamas
 */
public interface NexusItemAuthorizer
{
    /**
     * Authorizes a repository's path against an action.
     * 
     * @param repository
     * @param path
     * @return
     */
    boolean authorizePath( RepositoryItemUid uid, Map<String, Object> context, Action action );

    /**
     * Authorizes a root level ResourceStoreRequest against an Action.
     * 
     * @param request
     * @param rsr
     * @param action
     * @return
     */
    boolean authorizePath( ResourceStoreRequest rsr, Action action );

    /**
     * Authorizes an Event.
     * 
     * @param request
     * @param repository
     * @param action
     * @return
     */
    boolean authorizeInternalAction( ResourceStoreRequest request, Repository repository, InternalAction action );
}
