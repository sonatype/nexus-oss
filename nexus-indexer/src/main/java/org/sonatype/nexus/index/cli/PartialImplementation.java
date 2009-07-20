/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.cli;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An {@link java.lang.reflect.InvocationHandler} that can be extended with methods from the proxied interface.
 * While invocation it will look for a method within itself that matches the signature of the invoked peoxy method.
 * If found the method will be invoked and result returned, otherwise an {@link UnsupportedOperationException} will be
 * thrown.
 *
 * @author Alin Dreghiciu
 */
public class PartialImplementation
    implements InvocationHandler
{

    @Override
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        try
        {
            final Method localMethod = getClass().getMethod( method.getName(), method.getParameterTypes() );
            return localMethod.invoke( this, args );
        }
        catch( NoSuchMethodException e )
        {
            throw new UnsupportedOperationException( "Method " + method.getName() + "() is not supported" );
        }
        catch( IllegalAccessException e )
        {
            throw new UnsupportedOperationException( "Method " + method.getName() + "() is not supported" );
        }
        catch( InvocationTargetException e )
        {
            throw e.getCause();
        }
    }

}
