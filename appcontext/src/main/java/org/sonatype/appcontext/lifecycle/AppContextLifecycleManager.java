package org.sonatype.appcontext.lifecycle;

/**
 * App context lifecycle manager is very simplistic utility allowing any app context user to implement it's own
 * lifecycle. What triggers it, and what those are are left to implementors.
 * 
 * @author cstamas
 * @since 3.1
 */
public interface AppContextLifecycleManager
{
    /**
     * Method to register a handler.
     * 
     * @param handler
     */
    void registerManaged( LifecycleHandler handler );

    /**
     * Method to unregister a handler.
     * 
     * @param handler
     */
    void unregisterManaged( LifecycleHandler handler );

    /**
     * Invoke registered handlers for passed in class.
     * 
     * @param clazz
     */
    void invokeHandler( Class<? extends LifecycleHandler> clazz );
}
