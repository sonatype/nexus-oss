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
package org.sonatype.appcontext.guice;

import org.sonatype.appcontext.AppContext;

import com.google.inject.AbstractModule;

/**
 * Guice Module exposing AppContext as component, binding it to {@code AppContext.class} key.
 * 
 * @author cstamas
 * @since 3.1
 */
public class AppContextGuiceModule
    extends AbstractModule
{
    private final AppContext appContext;

    public AppContextGuiceModule( final AppContext appContext )
    {
        if ( appContext == null )
        {
            throw new NullPointerException( "AppContext instance cannot be null!" );
        }
        this.appContext = appContext;
    }

    protected AppContext getAppContext()
    {
        return appContext;
    }

    @Override
    protected void configure()
    {
        bind( AppContext.class ).toInstance( getAppContext() );
    }
}
