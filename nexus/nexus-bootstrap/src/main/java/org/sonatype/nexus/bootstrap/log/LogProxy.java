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

/**
 * A log proxy allowing redirecting output (e.g. in case that there is no slf4j available).
 *
 * @since 2.2
 */
public class LogProxy
{

    public void debug( final String message, Object... args )
    {
        // does nothing
    }

    public void info( final String message, final Object... args )
    {
        // does nothing
    }

    public void error( final String message, Object... args )
    {
        // does nothing
    }

    public void error( final String message, Throwable e )
    {
        // does nothing
    }

    public static LogProxy getLogger( final Class clazz )
    {
        try
        {
            LogProxy.class.getClassLoader().loadClass( "org.slf4j.Logger" );
            return new Slf4jLogProxy( clazz );
        }
        catch ( ClassNotFoundException e )
        {
            return new SystemOutLogProxy( clazz );
        }
    }

}
