/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Task state state machine.
 * 
 * @author cstamas
 */
public enum TState
{
    /**
     * Waits for run. Either because it's schedule is set in future or schedule queue is full.
     */
    SCHEDULED( TState.valueOf( "RUNNING" ), TState.valueOf( "BLOCKED" ) ),

    /**
     * Is currently running.
     */
    RUNNING( TState.valueOf( "SCHEDULED" ), TState.valueOf( "DONE" ), TState.valueOf( "BROKEN" ) ),

    /**
     * About to run, but was blocked (by some exclusion rule applied). This task will try to unblock in regular time
     * interval, and will run as soon as possible.
     */
    BLOCKED( TState.valueOf( "RUNNING" ) ),

    /**
     * Task is cleanly finished.
     */
    DONE(),

    /**
     * Task is broken.
     */
    BROKEN();

    private final Set<TState> nextHops;

    private TState( TState... hops )
    {
        LinkedHashSet<TState> nextHops = new LinkedHashSet<TState>();

        if ( hops != null )
        {
            for ( TState hop : hops )
            {
                nextHops.add( hop );
            }
        }

        this.nextHops = Collections.unmodifiableSet( nextHops );
    }

    /**
     * Returns true if this instance is ending state (nowhere else to transition to).
     * 
     * @return true if this is an ending state.
     */
    public boolean isEndingState()
    {
        return nextHops.isEmpty();
    }

    /**
     * Transitions "automatically" this state to next one if this state is not an ending state. In case of multiple
     * edges, the first one of them is chosen to transition to.
     * 
     * @return the next state where transition happened.
     * @throws IllegalStateException if this is an ending state.
     */
    public TState transition()
        throws IllegalStateException
    {
        if ( isEndingState() )
        {
            throw new IllegalStateException( "This \"" + this + "\" state is an ending state, it cannot transition." );
        }
        else
        {
            return transitionTo( nextHops.iterator().next() );
        }
    }

    /**
     * Transitions to specified nextState if it's valid transition.
     * 
     * @param nextState
     * @return nextState if it is valid.
     * @throws IllegalStateException if nextState is not valid transition.
     */
    public TState transitionTo( final TState nextState )
        throws IllegalStateException
    {
        if ( nextHops.contains( nextState ) )
        {
            return nextState;
        }
        else
        {
            throw new IllegalStateException( "State \"" + nextState + "\" is not in this \"" + this
                + "\" state's allowed transitions: " + nextHops.toString() );
        }

    }
}
