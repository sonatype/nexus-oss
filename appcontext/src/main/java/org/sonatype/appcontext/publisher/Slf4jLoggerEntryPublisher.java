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
package org.sonatype.appcontext.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A publisher that publishes Application Context to SLF4J Log on first enabled level (will try DEBUG, INFO, WARN in
 * this order). If none of those are enabled, will do nothing.
 * 
 * @author cstamas
 */
public class Slf4jLoggerEntryPublisher
    extends AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
    private final Logger logger;

    public Slf4jLoggerEntryPublisher()
    {
        this( LoggerFactory.getLogger( AppContext.class ) );
    }

    public Slf4jLoggerEntryPublisher( final Logger logger )
    {
        this.logger = Preconditions.checkNotNull( logger );
    }

    public void publishEntries( final AppContext context )
    {
        final String dump = "\n" + getDumpAsString( context );
        if ( logger.isDebugEnabled() )
        {
            logger.debug( dump );
        }
        else if ( logger.isInfoEnabled() )
        {
            logger.info( dump );
        }
        else if ( logger.isWarnEnabled() )
        {
            logger.warn( dump );
        }
    }
}
