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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.maven.wl.discovery.Strategy;

/**
 * Abstract class for {@link Strategy} implementations.
 * 
 * @author cstamas
 */
public abstract class AbstractStrategy
    extends AbstractLoggingComponent
    implements Strategy
{
    private final String id;

    private final int priority;

    protected AbstractStrategy( final String id, final int priority )
    {
        this.id = checkNotNull( id );
        this.priority = priority;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
}
