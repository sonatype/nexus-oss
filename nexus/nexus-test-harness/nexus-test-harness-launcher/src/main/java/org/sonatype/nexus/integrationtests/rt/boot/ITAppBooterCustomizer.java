package org.sonatype.nexus.integrationtests.rt.boot;

import java.io.File;

import org.sonatype.appbooter.AbstractPlexusAppBooterCustomizer;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.integrationtests.rt.prefs.FilePreferencesFactory;

public class ITAppBooterCustomizer
    extends AbstractPlexusAppBooterCustomizer
{
    public static final String TEST_ID_PREFIX = "testId=";

    @Override
    public void customizeContext( final PlexusAppBooter appBooter, final AppContext ctx )
    {
        String testId = getTestIdFromCmdLine( appBooter.getCommandLineArguments() );

        FilePreferencesFactory.setPreferencesFile( getFilePrefsFile( ctx.getBasedir(), testId ) );
    }

    protected String getTestIdFromCmdLine( String[] args )
    {
        // rather simple "parsing" of cmdLine
        for ( String arg : args )
        {
            if ( arg.startsWith( TEST_ID_PREFIX ) )
            {
                return arg.substring( TEST_ID_PREFIX.length() );
            }
        }

        return "unknown";
    }

    // ==

    public static File getFilePrefsFile( File dir, String testId )
    {
        return new File( dir, getFilePrefsFileName( testId ) );
    }

    public static String getFilePrefsFileName( String testId )
    {
        return testId + "-filePrefs";
    }

}
