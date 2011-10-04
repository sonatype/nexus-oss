/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nullificator
{
    private static Logger logger = LoggerFactory.getLogger( Nullificator.class );

    /**
     * Searches and nullifies passed in instance's member fields, using reflection. Will "eat" all exceptions, but also
     * log them.
     * 
     * @param instance
     */
    public static void nullifyMembers( final Object instance )
    {
        if ( instance == null )
        {
            return;
        }

        try
        {
            nullifyMembers( instance.getClass(), instance );
        }
        catch ( Exception e )
        {
            // catch all to not make test "skipped"
            logger.warn( String.format( "Could not nullify instance %s of class %s", instance.toString(),
                instance.getClass().getName() ), e );
        }
    }

    // ==

    /**
     * Searches and nullifies passed in instance and corresponding class. This method might throw some exception, and
     * should not accept {@code null} as any of it's parameters.
     * 
     * @param clazz
     * @param instance
     */
    public static void nullifyMembers( final Class<?> clazz, final Object instance )
    {
        Field[] fields = clazz.getDeclaredFields();

        for ( Field field : fields )
        {
            if ( !field.getType().isPrimitive() && !Modifier.isFinal( field.getModifiers() )
                && !Modifier.isStatic( field.getModifiers() ) )
            {
                field.setAccessible( true );

                try
                {
                    field.set( instance, null );
                }
                catch ( IllegalArgumentException e )
                {
                    // nop
                }
                catch ( IllegalAccessException e )
                {
                    // nop
                }
            }
        }

        if ( clazz.getSuperclass() != null )
        {
            nullifyMembers( clazz.getSuperclass(), instance );
        }
    }
}
