package org.sonatype.nexus.plugins.capabilities.internal.activation;

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCompositeCondition;

/**
 * A condition that applies a logical NOT on another condition.
 *
 * @since 1.10.0
 */
public class InversionCondition
    extends AbstractCompositeCondition
    implements Condition
{

    private final Condition condition;

    public InversionCondition( final ActivationContext activationContext,
                               final Condition condition )
    {
        super( activationContext, condition );
        this.condition = condition;
    }

    @Override
    protected boolean check( final Condition... conditions )
    {
        return !conditions[0].isSatisfied();
    }

    @Override
    public String toString()
    {
        return "NOT " + condition;
    }

}
