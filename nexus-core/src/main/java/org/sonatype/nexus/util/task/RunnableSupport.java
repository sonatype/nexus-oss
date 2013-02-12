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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Support class for {@link Cancelable} implementations of {@link Runnable} interfaces.
 * 
 * @author cstamas
 * @since 2.4
 */
public abstract class RunnableSupport
    implements Runnable, Cancelable
{
    private final Logger logger;

    private final ProgressListenerWrapper progressListenerWrapper;

    private final CancelableSupport cancelableSupport;

    protected RunnableSupport( final ProgressListener progressListener )
    {
        this.logger = LoggerFactory.getLogger( getClass() );
        this.progressListenerWrapper = new ProgressListenerWrapper( progressListener );
        this.cancelableSupport = new CancelableSupport();
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected ProgressListener getProgressListener()
    {
        return progressListenerWrapper;
    }

    protected void checkInterruption()
    {
        TaskUtil.checkInterruption( cancelableSupport );
    }

    @Override
    public boolean isCanceled()
    {
        return cancelableSupport.isCanceled();
    }

    @Override
    public void cancel()
    {
        cancelableSupport.cancel();
    }

    @Override
    public final void run()
    {
        if ( isCanceled() )
        {
            getLogger().debug( "Canceled before running, bailing out." );
            return;
        }
        final Cancelable old = TaskUtil.getCurrentCancelable();
        try
        {
            TaskUtil.setCurrentCancelable( cancelableSupport );
            getLogger().debug( "Running..." );
            doRun();
            getLogger().debug( "Done..." );
        }
        catch ( TaskInterruptedException e )
        {
            getLogger().info( "Interrupted: {}", e.getMessage() );
        }
        catch ( Exception e )
        {
            getLogger().warn( "Failed:", e );
            Throwables.propagate( e );
        }
        finally
        {
            TaskUtil.setCurrentCancelable( old );
        }
    }

    protected abstract void doRun();
}
