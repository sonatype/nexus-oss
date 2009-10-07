package org.sonatype.nexus.proxy.repository;

import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractWebSiteRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String WELCOME_FILES = "welcomeFiles";

    public AbstractWebSiteRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public List<String> getWelcomeFiles()
    {
        List<String> result = getCollection( getRootNode(), WELCOME_FILES );

        if ( result.isEmpty() )
        {
            // default it
            setCollection( getRootNode(), WELCOME_FILES, Arrays.asList( new String[] { "index.html", "index.htm" } ) );

            return getWelcomeFiles();
        }
        else
        {
            return result;
        }
    }

    public void setWelcomeFiles( List<String> vals )
    {
        setCollection( getRootNode(), WELCOME_FILES, vals );
    }
}
