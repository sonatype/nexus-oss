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
package org.sonatype.nexus.util.task;

/**
 * Runtime exception thrown in cases when task thread is interrupted or canceled. Semantical meaning is almost same as
 * {@link InterruptedException} meaning, except this one is unchecked exception and carries information about
 * cancellation state.
 * 
 * @author cstamas
 * @since 2.4
 */
@SuppressWarnings( "serial" )
public class TaskInterruptedException
    extends RuntimeException
{
    private final boolean canceled;

    /**
     * Constructor.
     * 
     * @param message the interruption message.
     * @param canceled {@code true} if interruption reason is cancelation.
     */
    public TaskInterruptedException( final String message, final boolean canceled )
    {
        super( message );
        this.canceled = canceled;
    }

    /**
     * Returns {@code true} if interruption reason was cancellation. In other case, {@link Thread#interrupt()} is the
     * reason of the interruption.
     * 
     * @return {@code true} if interruption reason was cancellation, {@code false} otherwise.
     */
    public boolean isCanceled()
    {
        return canceled;
    }
}
