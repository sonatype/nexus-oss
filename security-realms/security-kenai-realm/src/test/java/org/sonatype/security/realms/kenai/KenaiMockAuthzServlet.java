/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.kenai;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

public class KenaiMockAuthzServlet
    extends HttpServlet
{
    
    private int totalProjectSize = 301;
    
    public static String TOTAL_PROJECTS_KEY = "totalProjects";

    /**
     * Genearted serial uid.
     */
    private static final long serialVersionUID = -881495552351752305L;
    
    @Override
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );
        
        String totalProjectsParam = config.getInitParameter( TOTAL_PROJECTS_KEY );
        if( totalProjectsParam != null )
        {
            totalProjectSize = Integer.parseInt( totalProjectsParam );
        }
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException,
            IOException
    {
        
        int pageSize = 200;
        int pageIndex = 1;

        String username = req.getParameter( "username" );
        if( username == null )
        {
            resp.setStatus( 400 );
            return;
        }
        String[] roles = URLDecoder.decode( req.getParameter( "roles" ), "UTF8").split( "," );


        
        String sizeParam = req.getParameter( "size" );
        if( sizeParam != null )
        {
            pageSize = Integer.parseInt( sizeParam );
        }

        String pageIndexParam = req.getParameter( "page" );
        if( pageIndexParam != null )
        {
            pageIndex = Integer.parseInt( pageIndexParam );
        }
        
        String reqUrl = req.getRequestURL().substring( 0, req.getRequestURL().indexOf( "/api/" ) );
        
        
        try
        {
            String output = new KenaiProjectsJsonGenerator( pageSize, totalProjectSize, reqUrl ).generate( pageIndex, username, roles );
            resp.getOutputStream().write( output.getBytes() );
        }
        catch ( JSONException e )
        {
            this.log( "Failed to generate JSON", e );
            resp.setStatus( 500 );
        } 
    }    

}
