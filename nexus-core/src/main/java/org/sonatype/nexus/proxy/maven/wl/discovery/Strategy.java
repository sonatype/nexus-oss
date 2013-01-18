/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.discovery;

import java.util.Comparator;

/**
 * Strategy for discovery of WL by some means. It is identified by {@link #getId()} and has priority
 * {@link #getPriority()}. Latter is used to sort (using natural order of integers) the instances and try the one by one
 * in sorted order.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface Strategy
{
    /**
     * Returns the unique ID of the strategy, never {@code null}.
     * 
     * @return the ID of the strategy.
     */
    String getId();

    /**
     * Returns the priority of the strategy. Used in discovery process to order strategies in order to have them
     * executed. Less the priority, strategy will be earlier executed.
     * 
     * @return the priority of the strategy.
     */
    int getPriority();

    // ==

    /**
     * Comparator for {@link Strategy} instances.
     * 
     * @param <T>
     */
    public static class StrategyPriorityOrderingComparator<T extends Strategy>
        implements Comparator<T>
    {
        @Override
        public int compare( T o1, T o2 )
        {
            return o1.getPriority() - o2.getPriority();
        }
    }
}
