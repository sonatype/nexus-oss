/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * Default {@link ActivationContext} implementation.
 *
 * @since 1.10.0
 */
@Named
@Singleton
class DefaultActivationContext
    extends AbstractLoggingComponent
    implements ActivationContext
{

    /**
     * Set of listeners on all conditions. Never null.
     */
    private final Set<Listener> allConditionsListeners;

    /**
     * Map between listeners and conditions they are listening to. Never null.
     */
    private final Map<Condition, Set<Listener>> conditionListeners;

    @Inject
    DefaultActivationContext()
    {
        allConditionsListeners = new CopyOnWriteArraySet<Listener>();
        conditionListeners = new HashMap<Condition, Set<Listener>>();
    }

    @Override
    public ActivationContext addListener( final Listener listener, final Condition... conditions )
    {
        if ( conditions == null || conditions.length == 0 )
        {
            allConditionsListeners.add( listener );
            getLogger().debug( "Added listener {} for all conditions", listener );
        }
        else
        {
            for ( final Condition condition : conditions )
            {
                Set<Listener> listeners = conditionListeners.get( condition );
                if ( listeners == null )
                {
                    listeners = new CopyOnWriteArraySet<Listener>();
                    conditionListeners.put( condition, listeners );
                }
                listeners.add( listener );
            }
            getLogger().debug( "Added listener {} for conditions {}", listener, conditions );
        }

        return this;
    }

    @Override
    public ActivationContext removeListener( final Listener listener, final Condition... conditions )
    {
        if ( conditions == null || conditions.length == 0 )
        {
            allConditionsListeners.remove( listener );
            getLogger().debug( "Removed listener {} for all conditions", listener );
        }
        else
        {
            getLogger().debug( "Removed listener {} for conditions {}", listener, conditions );
            for ( final Condition condition : conditions )
            {
                final Set<Listener> listeners = conditionListeners.get( condition );
                if ( listeners != null )
                {
                    listeners.remove( listener );
                }
            }
        }

        return this;
    }

    @Override
    public ActivationContext notifySatisfied( final Condition condition )
    {
        getLogger().debug( "Condition {} has been satisfied", condition );
        notifySatisfied( checkNotNull( condition ), allConditionsListeners );
        notifySatisfied( condition, conditionListeners.get( condition ) );

        return this;
    }

    @Override
    public ActivationContext notifyUnsatisfied( final Condition condition )
    {
        getLogger().debug( "Condition {} has been unsatisfied", condition );
        notifyUnsatisfied( checkNotNull( condition ), allConditionsListeners );
        notifyUnsatisfied( condition, conditionListeners.get( condition ) );

        return this;
    }

    /**
     * Notifies listeners about a condition being satisfied.
     *
     * @param condition condition that was satisfied
     * @param listeners to be notified
     */
    private void notifySatisfied( final Condition condition, final Set<Listener> listeners )
    {
        if ( listeners != null )
        {
            getLogger().debug( "Notifying {} activation context listeners...", listeners.size() );
            for ( final Listener listener : listeners )
            {
                getLogger().debug( "Notifying listener {} about condition {} being satisfied", listener, condition );
                try
                {
                    listener.onSatisfied( condition );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        "Catched exception while notifying listener {} about condition {} being satisfied",
                        new Object[]{ listener, condition, e }
                    );
                }
            }
        }
    }

    /**
     * Notifies listeners about a condition being unsatisfied.
     *
     * @param condition condition that was unsatisfied
     * @param listeners to be notified
     */
    private void notifyUnsatisfied( final Condition condition, final Set<Listener> listeners )
    {
        if ( listeners != null )
        {
            getLogger().debug( "Notifying {} listeners...", listeners.size() );
            for ( final Listener listener : listeners )
            {
                getLogger().debug( "Notifying listener {} about condition {} being unsatisfied", listener, condition );
                try
                {
                    listener.onUnsatisfied( condition );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        "Catched exception while notifying listener {} about condition {} being unsatisfied",
                        new Object[]{ listener, condition, e }
                    );
                }
            }
        }
    }

}
