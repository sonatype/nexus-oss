package org.sonatype.nexus.proxy.access;

/**
 * Interface that represents the "internal actions", that happens as "side effects" of executing the given Action.
 * Example is the "proxy" action. When user requests a file from Nexus, it _may_ have to execute a "proxy" action (go
 * remote, cache locally and then serve). Another example is "outboundConnection", which happens as sub-part of proxy
 * event, when Nexus -- as side-effect of processing user's request -- have to initiate outbound connection to the
 * remote repository. "proxy" and "outboundConnection" are not the same!
 * 
 * @author cstamas
 */
public interface InternalAction
{
    String getPermissionString();
}
