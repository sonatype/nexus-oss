package org.sonatype.nexus.proxy.walker;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStore;

/**
 * The WalkerContext is usable to control the walk and to share some contextual data during the wak.
 * 
 * @author cstamas
 */
public interface WalkerContext
{
    /**
     * Returns the context.
     * 
     * @return
     */
    Map<String, Object> getContext();

    /**
     * Gets (and creates in null and empty list) the list of processors.
     * 
     * @return
     */
    List<WalkerProcessor> getProcessors();

    /**
     * Sets the list of processors to use.
     * 
     * @param processors
     */
    void setProcessors( List<WalkerProcessor> processors );

    /**
     * Stops the walker.
     */
    void stop();

    /**
     * Stops the walker with cause.
     * 
     * @param cause
     */
    void stop( Throwable cause );

    /**
     * Returns true is walker is stopped in the middle of walking.
     * 
     * @return
     */
    boolean isStopped();

    /**
     * Returns the cause of stopping this walker or null if none is given.
     * 
     * @return
     */
    Throwable getStopCause();

    /**
     * Returns the filter used in walk or null.
     * 
     * @return the used filter or null.
     */
    WalkerFilter getFilter();

    /**
     * Returns the resource store instance that is/will be walked over.
     * 
     * @return
     */
    ResourceStore getResourceStore();
}
