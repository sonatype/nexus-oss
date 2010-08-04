/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;

import org.sonatype.nexus.plugins.RepositoryType;

/**
 * Runtime implementation of Nexus @{@link RepositoryType} annotation.
 */
final class RepositoryTypeImpl
    implements RepositoryType
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String pathPrefix;

    private final int repositoryMaxInstanceCount;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    RepositoryTypeImpl( final String pathPrefix, final int repositoryMaxInstanceCount )
    {
        if ( null == pathPrefix )
        {
            throw new IllegalArgumentException( "@RepositoryType cannot contain null values" );
        }

        this.pathPrefix = pathPrefix;
        this.repositoryMaxInstanceCount = repositoryMaxInstanceCount;
    }

    // ----------------------------------------------------------------------
    // Annotation properties
    // ----------------------------------------------------------------------

    public String pathPrefix()
    {
        return pathPrefix;
    }

    public int repositoryMaxInstanceCount()
    {
        return repositoryMaxInstanceCount;
    }

    // ----------------------------------------------------------------------
    // Standard annotation behaviour
    // ----------------------------------------------------------------------

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }

        if ( rhs instanceof RepositoryType )
        {
            final RepositoryType type = (RepositoryType) rhs;

            return pathPrefix.equals( type.pathPrefix() )
                && repositoryMaxInstanceCount == type.repositoryMaxInstanceCount();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "pathPrefix".hashCode() ^ pathPrefix.hashCode() )
            + ( 127 * "repositoryMaxInstanceCount".hashCode() ^ Integer.valueOf( repositoryMaxInstanceCount ).hashCode() );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(pathPrefix=%s, repositoryMaxInstanceCount=%s)", RepositoryType.class.getName(),
                              pathPrefix, Integer.valueOf( repositoryMaxInstanceCount ) );
    }

    public Class<? extends Annotation> annotationType()
    {
        return RepositoryType.class;
    }
}