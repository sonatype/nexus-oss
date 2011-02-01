package org.sonatype.nexus.proxy.storage.remote.ahc;

import com.ning.http.client.Response;

public class AHCUtils
{
    public static boolean isAnyOfTheseStatusCodes( final Response response, int... codes )
    {
        for ( int code : codes )
        {
            if ( code == response.getStatusCode() )
            {
                return true;
            }
        }

        return false;
    }
}
