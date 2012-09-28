package org.sonatype.appcontext.lifecycle;

/**
 * Simple shared "startable" handler. You usually use this interface to create anonymous class (but you can freely
 * extend it too!) to handle some lifecycle here.
 * 
 * @author cstamas
 * @since 3.1
 */
public interface Startable
    extends LifecycleHandler
{

}
