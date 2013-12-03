/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.guice;

import java.lang.annotation.Annotation;

import org.codehaus.plexus.component.annotations.Component;

final class ComponentImpl
    implements Component
{
    private static final int HASH_CODE_OFFSET = 0x685499F5;

    private final Class<?> role;

    private final String hint;

    private final String instantiationStrategy;

    private final String description;

    public ComponentImpl( final Class<?> role, final String hint, final String instantiationStrategy,
                          final String description )
    {
        if ( null == role || null == hint || null == instantiationStrategy || null == description )
        {
            throw new IllegalArgumentException( "@Component cannot contain null values" );
        }

        this.role = role;
        this.hint = hint;
        this.instantiationStrategy = instantiationStrategy;
        this.description = description;
    }

    public Class<?> role()
    {
        return role;
    }

    public String hint()
    {
        return hint;
    }

    public String instantiationStrategy()
    {
        return instantiationStrategy;
    }

    public String description()
    {
        return description;
    }

    public boolean isolatedRealm()
    {
        return false;
    }

    public String alias()
    {
        return "";
    }

    public String composer()
    {
        return "";
    }

    public String configurator()
    {
        return "";
    }

    public String factory()
    {
        return "";
    }

    public String lifecycleHandler()
    {
        return "";
    }

    public String profile()
    {
        return "";
    }

    public String type()
    {
        return "";
    }

    public String version()
    {
        return "";
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }

        if ( rhs instanceof Component )
        {
            final Component cmp = (Component) rhs;

            if ( role.equals( cmp.role() ) && hint.equals( cmp.hint() )
                && instantiationStrategy.equals( cmp.instantiationStrategy() )
                && description.equals( cmp.description() ) )
            {
                // optimization: we hard-code all these attributes to be empty
                final String hardCodedAttributes =
                    cmp.alias() + cmp.composer() + cmp.configurator() + cmp.factory() + cmp.lifecycleHandler()
                        + cmp.profile() + cmp.type() + cmp.version();

                return hardCodedAttributes.length() == 0 && !cmp.isolatedRealm();
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return HASH_CODE_OFFSET + ( 127 * "role".hashCode() ^ role.hashCode() )
            + ( 127 * "hint".hashCode() ^ hint.hashCode() )
            + ( 127 * "instantiationStrategy".hashCode() ^ instantiationStrategy.hashCode() )
            + ( 127 * "description".hashCode() ^ description.hashCode() );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(isolatedRealm=false, composer=, configurator=, alias=, description=%s, "
            + "instantiationStrategy=%s, factory=, hint=%s, type=, lifecycleHandler=, version=, "
            + "profile=, role=%s)", Component.class.getName(), description, instantiationStrategy, hint, role );
    }

    public Class<? extends Annotation> annotationType()
    {
        return Component.class;
    }
}
