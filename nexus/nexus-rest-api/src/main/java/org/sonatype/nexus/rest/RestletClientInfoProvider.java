package org.sonatype.nexus.rest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.ClientInfoProvider;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

/**
 * {@link ClientInfoProvider} implementation that uses Security and Restlet frameworks to obtain informations. Note: in
 * case of indirect authentication (tokens), what will be returned as "userId" depends on the actual Realm
 * implementation used by given indirect authentication layer. So, it might be the indirect principal (the token) or the
 * userId of the indirectly authenticated user. Implementation dependent.
 * 
 * @author cstamas
 * @since 2.1
 */
@Component( role = ClientInfoProvider.class )
public class RestletClientInfoProvider
    extends AbstractLoggingComponent
    implements ClientInfoProvider
{
    @Override
    public ClientInfo getCurrentThreadClientInfo()
    {
        final Subject subject = SecurityUtils.getSubject();
        if ( subject != null && subject.getPrincipal() != null )
        {
            final String userId = subject.getPrincipal().toString();

            final Request current = Request.getCurrent();
            if ( current != null )
            {
                final String currentIp = RemoteIPFinder.findIP( current );
                final String currentUa = current.getClientInfo().getAgent();
                return new ClientInfo( userId, currentIp, currentUa );
            }
            else
            {
                // this is not HTTP processing thread at all
                return null;
            }
        }
        // we have no Shiro subject or "anonymous" user (from Shiro perspective, null principals
        return null;
    }
}
