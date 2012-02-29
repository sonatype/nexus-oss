package org.sonatype.appcontext.lifecycle;

/**
 * Root of the lifecycle handlers. Users of this "feature" should extend this interface, add semantics to their name and
 * implementations and use in a way they need.
 * 
 * @author cstamas
 */
public interface LifecycleHandler
{
    void handle();
}
