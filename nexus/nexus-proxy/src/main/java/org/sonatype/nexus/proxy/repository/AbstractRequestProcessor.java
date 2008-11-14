package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * A helper base class that makes it easier to create processors. Note: despite it's name, this class is not abstract
 * class.
 * 
 * @author cstamas
 */
public class AbstractRequestProcessor
    implements RequestProcessor
{

    public boolean process( Repository repository, ResourceStoreRequest request, Action action )
    {
        return true;
    }

    public boolean shouldProxy( Repository repository, RepositoryItemUid uid, Map<String, Object> context )
    {
        return true;
    }

}
