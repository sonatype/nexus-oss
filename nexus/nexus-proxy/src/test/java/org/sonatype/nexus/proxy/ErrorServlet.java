package org.sonatype.nexus.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.IOUtil;


public class ErrorServlet extends HttpServlet
{

    public static String CONTENT = "<html>some content</html>";
    
    private static Map<String, String> RESPONSE_HEADERS = new HashMap<String, String>();
    
    public static void clearHeaders()
    {
        RESPONSE_HEADERS.clear();
    }
    
    public static void addHeader( String key, String value )
    {
        RESPONSE_HEADERS.put( key, value );
    }
    
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        for ( Entry<String, String> headerEntry : RESPONSE_HEADERS.entrySet() )
        {
            resp.addHeader( headerEntry.getKey(), headerEntry.getValue() );   
        }
        
        IOUtil.copy( CONTENT, resp.getOutputStream() );
        
    }

}
