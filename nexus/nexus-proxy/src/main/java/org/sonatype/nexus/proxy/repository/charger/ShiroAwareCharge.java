package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

import org.apache.shiro.subject.Subject;

/**
 * Amount of parallel workload.
 * 
 * @author cstamas
 * @param <E>
 */
public class ShiroAwareCharge<E>
    extends Charge<E>
{
    private final Subject subject;

    public ShiroAwareCharge( final ChargeStrategy<E> strategy, final Subject subject )
    {
        super( strategy );

        this.subject = subject;
    }

    public void addAmmo( final Callable<? extends E> callable, final ExceptionHandler exceptionHandler )
    {
        super.addAmmo( subject.associateWith( callable ), exceptionHandler );
    }
}
