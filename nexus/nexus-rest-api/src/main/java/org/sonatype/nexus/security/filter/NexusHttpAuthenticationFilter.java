package org.sonatype.nexus.security.filter;

import static org.jsecurity.web.WebUtils.toHttp;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jsecurity.web.filter.authc.BasicHttpAuthenticationFilter;

public class NexusHttpAuthenticationFilter
    extends BasicHttpAuthenticationFilter
{
    // an override to avoid custom schemes and allow only HTTP Basic scheme
    protected boolean isLoginAttempt( ServletRequest request, ServletResponse response )
    {
        HttpServletRequest httpRequest = toHttp( request );

        String authorizationHeader = httpRequest.getHeader( AUTHORIZATION_HEADER );

        return authorizationHeader != null && authorizationHeader.toLowerCase().startsWith( "basic" );
    }
}
