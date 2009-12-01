package org.sonatype.nexus.buup.invoke;

/**
 * A component that invokes BUUP.
 * 
 * @author cstamas
 */
public interface NexusBuupInvoker
{
    void invokeBuup( NexusBuupInvocationRequest request )
        throws NexusBuupInvocationException;
}
