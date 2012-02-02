package org.sonatype.appcontext;

import java.io.PrintStream;
import java.util.Map;

import org.codehaus.plexus.interpolation.Interpolator;

/**
 * The generic app context, which is actually a Map.
 * 
 * @author cstamas
 */
public interface AppContext
    extends Map<String, Object>
{
    /**
     * Returns the id of this context.
     * 
     * @return
     */
    String getId();

    /**
     * Returns the parent app context if any, or {@code null} if this context is root context.
     * 
     * @return
     */
    AppContext getParent();

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map. This
     * map is not connected to AppContext anymore, and not modifiable! It is just a "snapshot".
     * 
     * @return
     */
    Map<String, Object> flatten();

    /**
     * Returns an interpolator using this app context as source.
     * 
     * @return
     */
    Interpolator getInterpolator();

    /**
     * Returns the entry value, used in creation of this context. Gives access to source marker and raw (uninterpolated)
     * values.
     * 
     * @return
     */
    AppContextEntry getAppContextEntry( String key );

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map but with
     * AppContextEntries as values. This map is not connected to AppContext anymore, and not modifiable! It is just a
     * "snapshot".
     * 
     * @return
     */
    Map<String, AppContextEntry> flattenAppContextEntries();

    /**
     * Dumps the complete AppContext (with hierarchy, sources) to default System.out.
     */
    void dump();

    /**
     * Dumps the complete AppContext (with hierarchy, sources) to given PrintStream.
     */
    void dump( PrintStream ps );
}
