/*
 * Copyright (c) 2007-2011 Sonatype, Inc. All rights reserved.
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

package org.sonatype.nexus.bootstrap.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs to SLF4J.
 *
 * @since 2.2
 */
public class Slf4jLogProxy
    extends LogProxy
{

    private Logger log = LoggerFactory.getLogger( this.getClass() );

    public Slf4jLogProxy( final Logger log )
    {
        this.log = log;
    }

    public Slf4jLogProxy( final Class clazz )
    {
        this( LoggerFactory.getLogger( clazz ) );
    }

    @Override
    public void debug( final String message, Object... args )
    {
        log.debug( message, args );
    }

    @Override
    public void info( final String message, final Object... args )
    {
        log.info( message, args );
    }

    @Override
    public void error( final String message, Object... args )
    {
        log.error( message, args );
    }

    @Override
    public void error( final String message, Throwable e )
    {
        log.error( message, e );
    }

}
