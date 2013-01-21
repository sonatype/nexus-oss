package org.sonatype.nexus.proxy;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import org.sonatype.nexus.proxy.ItemNotFoundReason.ItemNotFoundInRepositoryReason;
import org.sonatype.nexus.proxy.repository.Repository;

public class ItemNotFoundReasons
{
    public static class ItemNotFoundReasonImpl
        implements ItemNotFoundReason
    {
        private final String message;

        private final ResourceStoreRequest resourceStoreRequest;

        public ItemNotFoundReasonImpl( final String message, final ResourceStoreRequest resourceStoreRequest )
        {
            this.message = checkNotNull( message );
            this.resourceStoreRequest = checkNotNull( resourceStoreRequest );
        }

        @Override
        public String getMessage()
        {
            return message;
        }

        @Override
        public ResourceStoreRequest getResourceStoreRequest()
        {
            return resourceStoreRequest;
        }
    }

    public static class ItemNotFoundInRepositoryReasonImpl
        extends ItemNotFoundReasonImpl
        implements ItemNotFoundInRepositoryReason
    {
        private final Repository repository;

        public ItemNotFoundInRepositoryReasonImpl( final String message,
                                                   final ResourceStoreRequest resourceStoreRequest,
                                                   final Repository repository )
        {
            super( message, resourceStoreRequest );
            this.repository = checkNotNull( repository );
        }

        @Override
        public Repository getRepository()
        {
            return repository;
        }
    }

    // ==

    public static ItemNotFoundReason reasonFor( final ResourceStoreRequest request, final String message, final String... params )
    {
        return new ItemNotFoundReasonImpl( message, request );
    }

    public static ItemNotFoundReason reasonFor( final ResourceStoreRequest request, final Repository repository,
                                                final String message, final String... params )
    {
        if ( repository != null )
        {
            return new ItemNotFoundInRepositoryReasonImpl( message, request, repository );
        }
        else
        {
            return reasonFor( request, message );
        }
    }

    // ==

    /**
     * Legacy support.
     * 
     * @param message
     * @param request
     * @param repository
     * @return reason.
     * @deprecated Used for legacy support, new code should NOT use this method.
     */
    public static ItemNotFoundReason legacySupport( final String message, final ResourceStoreRequest request,
                                                    final Repository repository )
    {
        if ( repository != null )
        {
            return new ItemNotFoundInRepositoryReasonImpl( message, request, repository );
        }
        return new ItemNotFoundReasonImpl( message, request );
    }
}
