package org.sonatype.nexus.proxy;

/**
 * IllegalRequestException is thrown when an illegal request is tried against a ResourceStore.
 * 
 * @author cstamas
 */
public class IllegalRequestException
    extends IllegalOperationException
{
    private static final long serialVersionUID = -1683012685732920168L;

    private final ResourceStoreRequest request;

    public IllegalRequestException( ResourceStoreRequest request, String message )
    {
        super( message );

        this.request = request;
    }

    public ResourceStoreRequest getRequest()
    {
        return request;
    }

}
