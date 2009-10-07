package org.sonatype.nexus.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Form;
import org.restlet.data.Request;

public class RemoteIPFinder
{
    protected static final String FORWARD_HEADER = "X-Forwarded-For";
    
    public static String findIP( HttpServletRequest request )
    {
        String forwardedIP = getFirstForwardedIp( request.getHeader( FORWARD_HEADER ) );
        
        if ( forwardedIP != null )
        {
            return forwardedIP;
        }
        
        String remoteIP = request.getRemoteAddr();
        
        if ( remoteIP != null )
        {
            return remoteIP;
        }
        
        return null;
    }
    public static String findIP( Request request )
    {
        Form form = (Form) request.getAttributes().get("org.restlet.http.headers");
        
        String forwardedIP = getFirstForwardedIp( form.getFirstValue( FORWARD_HEADER ) );
        
        if ( forwardedIP != null )
        {
            return forwardedIP;
        }
        
        List<String> ipAddresses = request.getClientInfo().getAddresses();
        
        if ( ipAddresses.size() > 0 )
        {
            return ipAddresses.get( 0 );
        }
        
        return null;
    }
    
    protected static String getFirstForwardedIp( String forwardedFor )
    {
        if ( !StringUtils.isEmpty( forwardedFor ) )
        {
            String [] forwardedIps = forwardedFor.split( "," );
            
            return forwardedIps[0].trim();
        }
        
        return null;
    }
}
