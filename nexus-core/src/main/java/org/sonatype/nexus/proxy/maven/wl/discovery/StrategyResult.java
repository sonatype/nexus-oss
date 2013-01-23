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

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;

/**
 * The result of a strategy discovery.
 * 
 * @author cstamas
 */
public class StrategyResult
{
    private final String message;

    private final EntrySource entrySource;

    /**
     * Constructor.
     * 
     * @param message
     * @param entrySource
     */
    public StrategyResult( final String message, final EntrySource entrySource )
    {
        this.message = checkNotNull( message );
        this.entrySource = checkNotNull( entrySource );
    }

    /**
     * Returns strategy specific message (probably explaining how did it get the results).
     * 
     * @return the message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Returns the entry source, as a result of discovery.
     * 
     * @return entry source discovered by strategy.
     */
    public EntrySource getEntrySource()
    {
        return entrySource;
    }
}
