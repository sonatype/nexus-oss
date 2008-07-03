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
