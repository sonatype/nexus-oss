/**
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
package org.sonatype.appcontext;

import com.google.inject.AbstractModule;

/**
 * Guice Module exposing AppContext as component, binding it to {@code AppContext.class} key. Word of warning: this
 * class is not suitable in all cases, like Nexus for example, as the appcontext is at "top level" Jetty classpath where
 * no Guice exists. Hence, Nexus for example "reimplements" this same module to avoid class not found related problems.
 * It really depends how you use AppContext.
 * 
 * @author cstamas
 * @since 3.1
 */
public class AppContextModule
    extends AbstractModule
{
    private final AppContext appContext;

    public AppContextModule( final AppContext appContext )
    {
        if ( appContext == null )
        {
            throw new NullPointerException( "AppContext instance cannot be null!" );
        }
        this.appContext = appContext;
    }

    @Override
    protected void configure()
    {
        bind( AppContext.class ).toInstance( appContext );
        // hm, I dislike parameters anyway, one should @Inject this one above instead
        // bind(ParameterKeys.PROPERTIES).toInstance( appContext );
    }
}
