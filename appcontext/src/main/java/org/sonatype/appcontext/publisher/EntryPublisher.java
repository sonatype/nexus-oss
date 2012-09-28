package org.sonatype.appcontext.publisher;

import org.sonatype.appcontext.AppContext;

/**
 * EntryPublisher to publish the context. Is invoked at the very end, when AppContext is already constructed.
 * 
 * @author cstamas
 */
public interface EntryPublisher
{
    /**
     * Invoked to publish the AppContext.
     * 
     * @param context
     */
    void publishEntries( AppContext context );
}
