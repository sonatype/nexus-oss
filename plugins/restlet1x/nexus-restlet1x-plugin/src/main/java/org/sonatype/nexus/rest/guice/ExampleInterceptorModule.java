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

package org.sonatype.nexus.rest.guice;

import javax.inject.Named;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.sonatype.nexus.guice.AbstractInterceptorModule;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

import com.google.inject.matcher.Matchers;

@Named
public class ExampleInterceptorModule
    extends AbstractInterceptorModule
{
    @Override
    protected void configure()
    {
        System.err.println( "--- INSTALL EXAMPLE INTERCEPTORS ---" );

        // must bind an interceptor, otherwise module won't be bound and shared via plugin manager
        bindInterceptor( Matchers.subclassesOf( NexusIndexHtmlCustomizer.class ), Matchers.any(),
                         new MethodInterceptor()
                         {
                             @Override
                             public Object invoke( MethodInvocation mi )
                                 throws Throwable
                             {
                                 System.err.println( "---> " // parent is the original class
                                     + mi.getThis().getClass().getSuperclass().getSimpleName() + '.'
                                     + mi.getMethod().getName() );
                                 return mi.proceed();
                             }
                         } );
    }
}
