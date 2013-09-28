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
 * Abstract {@link ResourceLockMBean} implementation.
 */
abstract class AbstractResourceLockMBean
    extends StandardMBean
    implements ResourceLockMBean
{
    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    AbstractResourceLockMBean()
    {
        super( ResourceLockMBean.class, false );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected final String getParameterName( final MBeanOperationInfo op, final MBeanParameterInfo param, final int seq )
    {
        return op.getName().endsWith( "Resources" ) ? "thread id #" : "resource name";
    }
}
