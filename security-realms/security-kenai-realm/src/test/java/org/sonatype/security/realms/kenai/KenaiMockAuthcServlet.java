package org.sonatype.security.realms.kenai;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KenaiMockAuthcServlet
    extends HttpServlet
{

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        String output = "NOT USED";
        resp.getOutputStream().write( output.getBytes() );
    }

}
