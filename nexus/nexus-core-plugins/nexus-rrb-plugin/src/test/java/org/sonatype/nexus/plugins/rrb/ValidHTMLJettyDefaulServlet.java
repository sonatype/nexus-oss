package org.sonatype.nexus.plugins.rrb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.resource.Resource;
import org.mortbay.util.URIUtil;

public class ValidHTMLJettyDefaulServlet extends DefaultServlet
{

    
    /**
     * The default jetty implementation doesn't produce valid HTML, it misses the closing &lt;/A&gt; tag
     */
    protected void sendDirectory(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Resource resource,
                                 boolean parent)
    throws IOException
    {
        
        byte[] data=null;
        String base = URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH);
        String dir = resource.getListHTML(base,parent);
        
        if (dir==null)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
            "No directory");
            return;
        }
        
        // need to add in the missing </A>
        dir = dir.replaceAll( "&nbsp;</TD><TD ALIGN=right>", "</A>&nbsp;</TD><TD ALIGN=right>" );
        
        data=dir.getBytes("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }
    
}
