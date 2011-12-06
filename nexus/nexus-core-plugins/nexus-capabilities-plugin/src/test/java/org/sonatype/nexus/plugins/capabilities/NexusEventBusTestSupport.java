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
package org.sonatype.nexus.plugins.capabilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;

/**
 * Support for tests using event bus.
 *
 * @since 1.10.0
 */
public class NexusEventBusTestSupport
{

    protected NexusEventBus eventBus;

    protected List<Object> eventBusEvents;

    public void setUp()
        throws Exception
    {
        eventBus = mock( NexusEventBus.class );
        eventBusEvents = new ArrayList<Object>();

        doAnswer( new Answer<Object>()
        {

            @Override
            public Object answer( final InvocationOnMock invocation )
                throws Throwable
            {
                eventBusEvents.add( invocation.getArguments()[0] );
                return null;
            }

        } ).when( eventBus ).post( any() );
    }

    protected void verifyEventBusEvents( final Matcher... matchers )
    {
        assertThat( eventBusEvents, contains( matchers ) );
    }

    protected void verifyNoEventBusEvents( )
    {
        assertThat( eventBusEvents, empty() );
    }

    protected static Matcher<Object> satisfied( final Condition condition )
    {
        return allOf(
            instanceOf( ConditionEvent.Satisfied.class ),
            new ArgumentMatcher<Object>()
            {
                @Override
                public boolean matches( final Object argument )
                {
                    return ( (ConditionEvent.Satisfied) argument ).getCondition() == condition;
                }
            }
        );
    }

    protected static Matcher<Object> unsatisfied( final Condition condition )
    {
        return allOf(
            instanceOf( ConditionEvent.Unsatisfied.class ),
            new ArgumentMatcher<Object>()
            {
                @Override
                public boolean matches( final Object argument )
                {
                    return ( (ConditionEvent.Unsatisfied) argument ).getCondition() == condition;
                }
            }
        );
    }

}
