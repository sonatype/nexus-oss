/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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