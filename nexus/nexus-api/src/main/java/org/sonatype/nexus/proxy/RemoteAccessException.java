package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * Top level class for all remote related auth/authz problems.
 * 
 * @author cstamas
 */
public abstract class RemoteAccessException
    extends StorageException
{
    private static final long serialVersionUID = 391662938886542734L;

    private final ProxyRepository repository;

    public RemoteAccessException( ProxyRepository repository, String message )
    {
        this( repository, message, null );
    }

    public RemoteAccessException( ProxyRepository repository, String message, Throwable cause )
    {
        super( message, cause );

        this.repository = repository;
    }

    public ProxyRepository getRepository()
    {
        return repository;
    }

}
