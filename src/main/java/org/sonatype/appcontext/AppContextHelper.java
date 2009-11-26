package org.sonatype.appcontext;

import java.io.File;

public class AppContextHelper
{
    private AppContextHelperConfiguration appContextHelperConfiguration;

    /**
     * The basedir file. If null, discovery will be tried from system properties, otherwise this file will be used.
     */
    private File basedir = null;

    public AppContextHelper()
    {
        this( new AppContextHelperConfiguration() );
    }

    public AppContextHelper( AppContextHelperConfiguration configuration )
    {
        this.appContextHelperConfiguration = configuration;
    }

    public AppContextHelperConfiguration getConfiguration()
    {
        if ( appContextHelperConfiguration == null )
        {
            appContextHelperConfiguration = new AppContextHelperConfiguration();
        }

        return appContextHelperConfiguration;
    }

    public void setConfiguration( AppContextHelperConfiguration appContextHelperConfiguration )
    {
        this.appContextHelperConfiguration = appContextHelperConfiguration;
    }

    /**
     * Returns the File pointing to the basedir. If it is not set in System properties, it will try to "guess" (and
     * probably give wrong results, as this method is copied from PlexusTestCase class).
     * 
     * @return
     */
    public File getBasedir()
    {
        if ( basedir != null )
        {
            return basedir;
        }

        String basedirPath = System.getProperty( getConfiguration().getBasedirPropertyKey() );

        if ( basedirPath == null )
        {
            basedir = new File( "" ).getAbsoluteFile();
        }
        else
        {
            basedir = new File( basedirPath ).getAbsoluteFile();
        }

        return basedir;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }
}
