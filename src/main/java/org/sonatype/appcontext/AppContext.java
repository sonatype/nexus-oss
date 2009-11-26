package org.sonatype.appcontext;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.interpolation.Interpolator;

/**
 * The generic app context, which is actually a Map.
 * 
 * @author cstamas
 */
public interface AppContext
    extends Map<Object, Object>
{
    /**
     * Returns the factory that created this context.
     * 
     * @return
     */
    AppContextFactory getFactory();

    /**
     * Returns the name of this context.
     * 
     * @return
     */
    String getName();

    /**
     * Returns the basedir of this context.
     * 
     * @return
     */
    File getBasedir();

    /**
     * Returns the uninterpolated values, used in creation of this context.
     * 
     * @return
     */
    Map<Object, Object> getRawContext();

    /**
     * Returns an interpolator using this app context as source.
     * 
     * @return
     */
    Interpolator getInterpolator();
}
