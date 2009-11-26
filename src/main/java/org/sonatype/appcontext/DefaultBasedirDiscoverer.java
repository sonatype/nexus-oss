package org.sonatype.appcontext;

/**
 * Default implemenetation of basedir discovery strategy, that is blatantly copied from Plexus codebase. It expects a
 * JVM system propertiy named "basedir" to be present with a file path to the base directory as value. If not found,
 * will try to guess and use current JVM directory, which is usually NOT what you want.
 * 
 * @author cstamas
 */
public class DefaultBasedirDiscoverer
    extends AbstractSystemPropertiesBasedirDiscoverer
{
    public DefaultBasedirDiscoverer()
    {
        super();

        setBasedirKey( "basedir" );
    }
}
