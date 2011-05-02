package org.sonatype.nexus.proxy.utils;

import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryStringUtils
{
    public static String getFormattedMessage( final String string, final Repository repository )
    {
        return String.format( string, getHumanizedNameString( repository ) );
    }

    public static String getHumanizedNameString( final Repository repository )
    {
        return String.format( "\"%s\" [id=%s]", repository.getName(), repository.getId() );
    }
}
