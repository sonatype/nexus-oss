package org.sonatype.appcontext;

import java.io.File;

/**
 * Abstract implemenetation of basedir discovery strategy using JVM system properties. It expects a JVM system property
 * (name of the key is configurable) to be present with a file path to the base directory as value. If not found, will
 * try to guess and use current JVM directory, which is usually NOT what you want.
 * 
 * @author cstamas
 */
public abstract class AbstractSystemPropertiesBasedirDiscoverer
    implements BasedirDiscoverer
{
    private String basedirKey;

    public String getBasedirKey()
    {
        return basedirKey;
    }

    public void setBasedirKey( String basedirKey )
    {
        this.basedirKey = basedirKey;
    }

    public File discoverBasedir()
    {
        File basedir = null;

        String basedirPath = System.getProperty( getBasedirKey() );

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

}
