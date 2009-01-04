package org.sonatype.nexus.proxy;

import java.net.URL;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * Thrown when a request is denied by remote peer for security reasons (ie. HTTP RemoteRepositoryStorage gets 403
 * response code).
 * 
 * @author cstamas
 */
public class RemoteAccessDeniedException
    extends RemoteAccessException
{
    private static final long serialVersionUID = -4719375204384900503L;

    private final URL url;

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message )
    {
        this( repository, url, message, null );
    }

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message, Throwable cause )
    {
        super( repository, message, cause );

        this.url = url;
    }

    public URL getUrl()
    {
        return url;
    }

}
