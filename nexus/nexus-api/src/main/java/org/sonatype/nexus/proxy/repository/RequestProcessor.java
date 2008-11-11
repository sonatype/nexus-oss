package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;

/**
 * A Processor that is able to process/modify the request before Nexus will serve it.
 * 
 * @author cstamas
 */
public interface RequestProcessor
{
    /**
     * A method that is able to modify the request _after_ it is authorized, but before it is executed. If the method
     * wants to completely reject this request, AccessDeniedException should be thrown.
     * 
     * @param request
     * @param action
     * @throws AccessDeniedException
     */
    void process( ResourceStoreRequest request, Action action )
        throws AccessDeniedException;
}
