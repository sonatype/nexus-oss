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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.guice.bean.binders.ParameterKeys;

/**
 * Sisu Module, that extends {@link AppContextGuiceModule}, and adds one bit of salt: SISU's
 * {@link ParameterKeys#PROPERTIES} support. This means, that fields having {@link Inject} and {@link Named} having
 * value "aKey" annotations, will have AppContext's "aKey" entry injected.
 * 
 * @author cstamas
 * @since 3.1
 */
public class AppContextSisuModule
    extends AppContextGuiceModule
{
    public AppContextSisuModule( final AppContext appContext )
    {
        super( appContext );
    }

    @Override
    protected void configure()
    {
        super.configure();
        bind( ParameterKeys.PROPERTIES ).toInstance( new StringAdapter( getAppContext() ) );
    }

    // ==

    public static AppContextSisuModule withId( final String contextId, final File configurationDirectory )
    {
        return withIdAndParent( contextId, configurationDirectory, null );
    }

    public static AppContextSisuModule withIdAndParent( final String contextId, final File configurationDirectory,
                                                        final AppContext parent )
    {
        // create a default request with display-name as ID, possible parent (might be null)
        final AppContextRequest request = Factory.getDefaultRequest( contextId, parent );
        // add default context source as least important, as it leave room for override
        request.getSources().add( 0,
            new PropertiesFileEntrySource( new File( configurationDirectory, contextId + ".properties" ), false ) );
        // create an appcontext instance
        final AppContext appContext = Factory.create( request );
        // return
        return new AppContextSisuModule( appContext );
    }
}
