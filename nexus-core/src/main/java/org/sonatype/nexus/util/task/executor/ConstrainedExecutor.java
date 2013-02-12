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
package org.sonatype.nexus.util.task.executor;

import org.sonatype.nexus.util.task.CancelableRunnable;

/**
 * Simple {@link java.util.concurrent.Executor} like service, that offers a bit extra functionality in a way it can
 * guarantee no two concurrent commands are running under same key.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface ConstrainedExecutor
{
    /**
     * Returns statistics about this instance.
     * 
     * @return statistics.
     */
    Statistics getStatistics();

    /**
     * Schedules a command for execution, or, if a command with given key already runs, will simply "forget" (do
     * nothing) with passed in command instance.
     * 
     * @param key
     * @param command
     * @return {@code true} if command was scheduled, or {@code false} if dropped.
     */
    boolean mayExecute( String key, CancelableRunnable command );

    /**
     * Schedules a command for execution. If command with given key already runs, it will cancel it, and replace them.
     * 
     * @param key
     * @param command
     * @return {@code true} if this command caused a cancelation of other already scheduled command, or {@code false} if
     *         not.
     */
    boolean mustExecute( String key, CancelableRunnable command );
}
