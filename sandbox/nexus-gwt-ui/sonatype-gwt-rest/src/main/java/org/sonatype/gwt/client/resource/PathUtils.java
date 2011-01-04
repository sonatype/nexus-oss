/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.gwt.client.resource;

/**
 * Some common path utilities.
 * 
 * @author cstamas
 */
public class PathUtils
{

    private PathUtils()
    {
        // hidden utils
    }

    public static String append( String base, String path )
    {
        if ( path.startsWith( "/" ) )
        {
            return normalize( path );
        }
        else
        {
            String[] pathElems = path.split( "/" );

            for ( int i = 0; i < pathElems.length; i++ )
            {
                base = appendSingleElem( base, pathElems[i] );
            }

            return normalize( base );
        }
    }

    public static String normalize( String path )
    {
        String result = "/";

        String[] pathElems = path.split( "/" );

        for ( int i = 0; i < pathElems.length; i++ )
        {
            result = appendSingleElem( result, pathElems[i] );
        }
        return result;
    }

    public static String appendSingleElem( String base, String pathElem )
    {
        if ( "..".equals( pathElem ) )
        {
            if ( !"/".equals( base ) )
            {
                return base.substring( 0, base.lastIndexOf( '/' ) );
            }
            else
            {
                return "/";
            }
        }
        if ( "/".equals( base ) )
        {
            return "/" + pathElem;
        }
        else
        {
            return base + "/" + pathElem;
        }
    }

    public static UrlElements parseUrl( String url )
        throws IllegalArgumentException
    {
        if ( url.indexOf( "://" ) > -1 )
        {
            // this is URL
            // http://localhost[:port][/some/path]
            // parse it into: scheme, host, port and path

            String scheme = url.substring( 0, url.indexOf( "://" ) );

            url = url.substring( url.indexOf( "://" ) + 3, url.length() );

            String hostname = null;

            String port = null;

            if ( url.indexOf( ":" ) > -1 )
            {
                // we have port
                hostname = url.substring( 0, url.indexOf( ":" ) );

                url = url.substring( url.indexOf( ":" ) + 1, url.length() );

                port = url.substring( 0, url.indexOf( "/" ) );
            }
            else
            {
                // we have no port
                port = null;

                if ( url.indexOf( "/" ) == -1 )
                {
                    // we have no path either
                    hostname = url;

                    url = "/";
                }
                else
                {
                    // we have path
                    hostname = url.substring( 0, url.indexOf( "/" ) );

                    url = url.substring( url.indexOf( "/" ), url.length() );
                }
            }

            url = url.substring( url.indexOf( "/" ), url.length() );

            String path = PathUtils.normalize( url );

            return new UrlElements( scheme, hostname, port, path );
        }
        else
        {
            throw new IllegalArgumentException( "This is not an URL: " + url );
        }
    }

    public static class UrlElements
    {
        private String scheme;

        private String hostname;

        private String port;

        private String path;

        protected UrlElements( String scheme, String hostname, String port, String path )
        {
            this.scheme = scheme;

            this.hostname = hostname;

            this.port = port;

            this.path = path;
        }

        public String getScheme()
        {
            return scheme;
        }

        public String getHostname()
        {
            return hostname;
        }

        public String getPort()
        {
            return port;
        }

        public String getPath()
        {
            return path;
        }
    }

}
