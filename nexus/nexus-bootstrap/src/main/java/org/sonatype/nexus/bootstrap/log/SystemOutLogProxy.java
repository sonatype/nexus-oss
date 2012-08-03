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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs to System.out.
 *
 * @since 2.2
 */
public class SystemOutLogProxy
    extends LogProxy
{

    private Class clazz;

    public SystemOutLogProxy( final Class clazz )
    {
        this.clazz = clazz;
    }

    @Override
    public void debug( final String message, Object... args )
    {
        String timestamp = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() );
        System.out.println(
            timestamp + " " + clazz.getSimpleName() + " - " + String.format( message.replace( "{}", "%s" ), args )
        );
    }

    @Override
    public void info( final String message, final Object... args )
    {
        debug( message, args );
    }

    @Override
    public void error( final String message, final Throwable e )
    {
        error( message );
        e.printStackTrace( System.out );
    }

    @Override
    public void error( final String message, Object... args )
    {
        debug( message, args );
    }

}
