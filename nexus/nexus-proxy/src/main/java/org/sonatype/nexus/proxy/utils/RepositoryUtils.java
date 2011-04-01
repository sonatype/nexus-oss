package org.sonatype.nexus.proxy.utils;

import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryUtils
{
    public static String getLoggedNameString( final Repository repository )
    {
        return String.format( "\"%s\" [id=%s]", repository.getName(), repository.getId() );
    }
}
