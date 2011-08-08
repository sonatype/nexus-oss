package org.sonatype.nexus.integrationtests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Nullificator
{
    public static void nullifyMembers( final Object instance )
    {
        nullifyMembers( instance.getClass(), instance );
    }

    // ==

    protected static void nullifyMembers( final Class<?> clazz, final Object instance )
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
