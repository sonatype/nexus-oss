package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * A Processor that is able to process/modify the request before Nexus will serve it.
 * 
 * @author cstamas
 */
public interface RequestProcessor
{
    /**
     * A method that is able to modify the request _after_ it is authorized, but before it is executed. If the method
     * wants to completely stop the execution of this request, it should return false. Otherwise, true should be
     * returned.
     * 
     * @param request
     * @param action
     * @throws AccessDeniedException
     */
    boolean process( Repository repository, ResourceStoreRequest request, Action action );

    /**
     * Request processor is able to override generic behaviour of Repositories in aspect of proxying.
     * 
     * @param repository
     * @param uid
     * @param context
     * @return
     */
    boolean shouldProxy( Repository repository, RepositoryItemUid uid, Map<String, Object> context );
}
