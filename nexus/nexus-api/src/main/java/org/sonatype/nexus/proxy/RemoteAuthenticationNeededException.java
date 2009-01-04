package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * Thrown when a request need authentication by remote peer for security reasons (ie. HTTP RemoteRepositoryStorage gets
 * 401 response code with a challenge).
 * 
 * @author cstamas
 */
public class RemoteAuthenticationNeededException
    extends RemoteAccessException
{
    private static final long serialVersionUID = -7702305441562438729L;

    public RemoteAuthenticationNeededException( ProxyRepository repository, String message )
    {
        super( repository, message );
    }
}
