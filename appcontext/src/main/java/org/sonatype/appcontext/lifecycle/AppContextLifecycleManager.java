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
    void registerManaged( LifecycleHandler handler );

    void unregisterManaged( LifecycleHandler handler );

    void invokeHandler( Class<? extends LifecycleHandler> clazz );
}
