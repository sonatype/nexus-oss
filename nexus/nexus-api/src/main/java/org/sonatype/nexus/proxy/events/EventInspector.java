package org.sonatype.nexus.proxy.events;

/**
 * A simple event inspector, a component that receives events emitted by Nexus and processes them in way they want.
 * 
 * @author cstamas
 */
public interface EventInspector
{
    boolean accepts( AbstractEvent evt );

    void inspect( AbstractEvent evt );
}
