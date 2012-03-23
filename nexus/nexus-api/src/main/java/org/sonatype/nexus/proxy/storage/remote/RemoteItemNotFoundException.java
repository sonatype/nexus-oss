package org.sonatype.nexus.proxy.storage.remote;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * Thrown by RemoteRepositoryStorage if the requested item is not found for some reason that is part of internal
 * implementation of that same RemoteRepositoryStorage.
 * 
 * @author cstamas
 * @since 2.1
 */
public class RemoteItemNotFoundException
    extends ItemNotFoundException
{
    private static final long serialVersionUID = 8422409141417737154L;

    /**
     * Creates a "not found" exception with customized message, where RemoteRepositoryStorage may explain why it throw
     * this exception.
     * 
     * @param message
     * @param request
     * @param repository
     */
    public RemoteItemNotFoundException( final String message, final ResourceStoreRequest request,
                                        final ProxyRepository repository )
    {
        super( message, request, repository );
    }

    @Override
    public ProxyRepository getRepository()
    {
        return (ProxyRepository) super.getRepository();
    }
}
