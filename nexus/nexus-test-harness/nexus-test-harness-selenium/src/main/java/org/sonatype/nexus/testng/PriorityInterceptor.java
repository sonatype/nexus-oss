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
package org.sonatype.nexus.testng;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class PriorityInterceptor
    implements IMethodInterceptor
{

    public List<IMethodInstance> intercept( List<IMethodInstance> methods, ITestContext context )
    {
        Comparator<IMethodInstance> comparator = new Comparator<IMethodInstance>()
        {

            private int getPriority( IMethodInstance mi )
            {
                int result = 0;
                Method method = mi.getMethod().getMethod();
                Priority a1 = method.getAnnotation( Priority.class );
                if ( a1 != null )
                {
                    result = a1.value();
                }
                else
                {
                    Class<?> cls = method.getDeclaringClass();
                    Priority classPriority = cls.getAnnotation( Priority.class );
                    if ( classPriority != null )
                    {
                        result = classPriority.value();
                    }
                }
                return result;
            }

            public int compare( IMethodInstance m1, IMethodInstance m2 )
            {
                return getPriority( m1 ) - getPriority( m2 );
            }

        };
        IMethodInstance[] array = methods.toArray( new IMethodInstance[methods.size()] );
        Arrays.sort( array, comparator );

        return Arrays.asList( array );
    }

}