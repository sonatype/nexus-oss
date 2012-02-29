package org.sonatype.appcontext.lifecycle;

/**
 * Simple shared "stoppable" handler. You usually use this interface to create anonymous class (bot you can freely
 * extend it too!) to handle some lifecycle here.
 * 
 * @author cstamas
 */
public interface Stoppable
    extends LifecycleHandler
{

}
