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
/**
 *
 */
package org.sonatype.nexus.repository.yum.internal.config;

/**
 * @author BVoss
 */
public class AliasKey
{
    private final String repoId;

    private final String alias;

    public AliasKey( String repoId, String alias )
    {
        this.repoId = repoId;
        this.alias = alias;
    }

    public String getRepoId()
    {
        return repoId;
    }

    public String getAlias()
    {
        return alias;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = ( prime * result ) + ( ( alias == null ) ? 0 : alias.hashCode() );
        result = ( prime * result ) + ( ( repoId == null ) ? 0 : repoId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
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

        AliasKey other = (AliasKey) obj;
        if ( alias == null )
        {
            if ( other.alias != null )
            {
                return false;
            }
        }
        else if ( !alias.equals( other.alias ) )
        {
            return false;
        }
        if ( repoId == null )
        {
            if ( other.repoId != null )
            {
                return false;
            }
        }
        else if ( !repoId.equals( other.repoId ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "AliasKey [repoId=" + repoId + ", alias=" + alias + "]";
    }
}
