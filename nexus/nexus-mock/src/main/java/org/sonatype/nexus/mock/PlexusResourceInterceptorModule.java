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
package org.sonatype.nexus.mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.sonatype.plexus.rest.resource.PlexusResource;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

@Named
final class PlexusResourceInterceptorModule
    extends AbstractModule
{
    static final List<String> INTERCEPTED_METHODS = Arrays.asList( "get", "delete", "put", "post", "upload" );

    @Override
    protected void configure()
    {
        bindInterceptor( Matchers.subclassesOf( PlexusResource.class ), new AbstractMatcher<Method>()
        {
            public boolean matches( Method m )
            {
                return INTERCEPTED_METHODS.contains( m.getName() );
            }
        }, new PlexusResourceInterceptor() );
    }
}
