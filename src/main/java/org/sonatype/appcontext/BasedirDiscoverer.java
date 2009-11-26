package org.sonatype.appcontext;

import java.io.File;

/**
 * Externalized strategy how to discover "basedir".
 * 
 * @author cstamas
 */
public interface BasedirDiscoverer
{
    File discoverBasedir();
}
