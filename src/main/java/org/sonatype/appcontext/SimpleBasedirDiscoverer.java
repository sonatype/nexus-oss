package org.sonatype.appcontext;

import java.io.File;

/**
 * A simple "discoverer" that actually just passes back the File it got. Usable in Tests, but in other situations too.
 * 
 * @author cstamas
 */
public class SimpleBasedirDiscoverer
    implements BasedirDiscoverer
{
    private final File basedir;

    public SimpleBasedirDiscoverer( File basedir )
    {
        this.basedir = basedir;
    }

    public File discoverBasedir()
    {
        return basedir;
    }

}
