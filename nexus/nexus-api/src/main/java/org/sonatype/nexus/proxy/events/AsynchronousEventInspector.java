package org.sonatype.nexus.proxy.events;

/**
 * A simple async event inspector marker interface. If this interface is present on an EventInspector implementor, it
 * becomes asynchornous, a component that receives events emitted by Nexus and processes them in way they want (async
 * invocation is managed by event inspector host, the implementor does not have to manage it).
 * 
 * @author cstamas
 */
public interface AsynchronousEventInspector
{

}
