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
package org.sonatype.nexus.plugins.p2.repository;

import java.util.Map;

public class P2MetadataGeneratorConfiguration
{

    public static final String REPO_OR_GROUP_ID = "repoOrGroup";

    private final String repositoryId;

    public P2MetadataGeneratorConfiguration( final Map<String, String> properties )
    {
        repositoryId = repository( properties );
    }

    public String repositoryId()
    {
        return repositoryId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final P2MetadataGeneratorConfiguration other = (P2MetadataGeneratorConfiguration) obj;
        if ( repositoryId == null )
        {
            if ( other.repositoryId != null )
            {
                return false;
            }
        }
        else if ( !repositoryId.equals( other.repositoryId ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( "P2MetadataGeneratorConfiguration [" );
        if ( repositoryId != null )
        {
            builder.append( "repositoryId=" );
            builder.append( repositoryId );
        }
        builder.append( "]" );
        return builder.toString();
    }

    private static String repository( final Map<String, String> properties )
    {
        String repositoryId = properties.get( REPO_OR_GROUP_ID );
        repositoryId = repositoryId.replaceFirst( "repo_", "" );
        repositoryId = repositoryId.replaceFirst( "group_", "" );
        return repositoryId;
    }

}
