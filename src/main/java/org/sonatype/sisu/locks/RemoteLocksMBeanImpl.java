/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.StandardMBean;

/**
 * Remote {@link LocksMBean} implementation.
 */
final class RemoteLocksMBeanImpl
    extends StandardMBean
    implements LocksMBean
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    RemoteLocksMBeanImpl()
    {
        super( LocksMBean.class, false );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String[] getResourceNames()
    {
        throw new UnsupportedOperationException();
    }

    public String[] getOwningThreads( final String name )
    {
        throw new UnsupportedOperationException();
    }

    public String[] getWaitingThreads( final String name )
    {
        throw new UnsupportedOperationException();
    }

    public String[] getOwnedResources( final String tid )
    {
        throw new UnsupportedOperationException();
    }

    public String[] getWaitedResources( final String tid )
    {
        throw new UnsupportedOperationException();
    }

    public void releaseResource( final String name )
    {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected String getParameterName( final MBeanOperationInfo op, final MBeanParameterInfo param, final int seq )
    {
        return op.getName().endsWith( "Resources" ) ? "thread id #" : "resource name";
    }
}
