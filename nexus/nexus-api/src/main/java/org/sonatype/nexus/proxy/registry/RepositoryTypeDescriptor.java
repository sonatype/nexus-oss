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
package org.sonatype.nexus.proxy.registry;

import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugins.RepositoryType;

/**
 * A simple descriptor for all roles implementing a Nexus Repository.
 * 
 * @author cstamas
 */
public class RepositoryTypeDescriptor
{
    private final String role;

    private final String hint;

    private final String prefix;

    private final int repositoryMaxInstanceCount;

    private AtomicInteger instanceCount = new AtomicInteger( 0 );

    public RepositoryTypeDescriptor( String role, String hint, String prefix )
    {
        this( role, hint, prefix, RepositoryType.UNLIMITED_INSTANCES );
    }

    public RepositoryTypeDescriptor( String role, String hint, String prefix, int repositoryMaxInstanceCount )
    {
        this.role = role;

        this.hint = hint;

        this.prefix = prefix;

        this.repositoryMaxInstanceCount = repositoryMaxInstanceCount;
    }

    public String getRole()
    {
        return role;
    }

    public String getHint()
    {
        return hint;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public int getRepositoryMaxInstanceCount()
    {
        return repositoryMaxInstanceCount;
    }

    public int getInstanceCount()
    {
        return instanceCount.get();
    }

    public int instanceRegistered( RepositoryRegistry registry )
    {
        return instanceCount.incrementAndGet();
    }

    public int instanceUnregistered( RepositoryRegistry registry )
    {
        return instanceCount.decrementAndGet();
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || ( o.getClass() != this.getClass() ) )
        {
            return false;
        }

        RepositoryTypeDescriptor other = (RepositoryTypeDescriptor) o;

        return StringUtils.equals( getRole(), other.getRole() ) && StringUtils.equals( getHint(), other.getHint() )
            && StringUtils.equals( getPrefix(), other.getPrefix() );
    }

    public int hashCode()
    {
        int result = 7;

        result = 31 * result + ( role == null ? 0 : role.hashCode() );

        result = 31 * result + ( hint == null ? 0 : hint.hashCode() );

        result = 31 * result + ( prefix == null ? 0 : prefix.hashCode() );

        return result;
    }

    public String toString()
    {
        return "RepositoryType=(" + getRole() + ":" + getHint() + ")";
    }
}
