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
package org.sonatype.nexus.repository.yum.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA. User: MKrautz Date: 7/8/11 Time: 3:36 PM To change this template use File | Settings | File
 * Templates.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement( name = "configuration" )
public class YumConfiguration
{

    protected static final int DEFAULT_TIMEOUT_IN_SEC = 120;

    protected static final boolean DEFAULT_REPOSITORY_OF_REPOSITORY_VERSIONS = true;

    protected static final int DEFAULT_MAX_PARALLEL_THREAD_COUNT = 10;

    private boolean active = true;

    private int repositoryCreationTimeout = DEFAULT_TIMEOUT_IN_SEC;

    private boolean repositoryOfRepositoryVersionsActive = DEFAULT_REPOSITORY_OF_REPOSITORY_VERSIONS;

    private int maxParallelThreadCount = DEFAULT_MAX_PARALLEL_THREAD_COUNT;

    public YumConfiguration()
    {
        super();
    }

    public YumConfiguration( final YumConfiguration other )
    {
        setActive( other.isActive() );
        repositoryCreationTimeout = other.getRepositoryCreationTimeout();
        repositoryOfRepositoryVersionsActive = other.isRepositoryOfRepositoryVersionsActive();
        maxParallelThreadCount = other.getMaxParallelThreadCount();
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public int getRepositoryCreationTimeout()
    {
        return repositoryCreationTimeout;
    }

    public void setRepositoryCreationTimeout( int repositoryCreationTimeout )
    {
        this.repositoryCreationTimeout = repositoryCreationTimeout;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = ( prime * result ) + repositoryCreationTimeout;
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

        YumConfiguration other = (YumConfiguration) obj;
        if ( repositoryCreationTimeout != other.repositoryCreationTimeout )
        {
            return false;
        }
        if ( repositoryOfRepositoryVersionsActive != other.repositoryOfRepositoryVersionsActive )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "XmlYumConfiguration [repositoryCreationTimeout=" + repositoryCreationTimeout;
    }

    public boolean isRepositoryOfRepositoryVersionsActive()
    {
        return repositoryOfRepositoryVersionsActive;
    }

    public void setRepositoryOfRepositoryVersionsActive( boolean repositoryOfRepositoryVersionsActive )
    {
        this.repositoryOfRepositoryVersionsActive = repositoryOfRepositoryVersionsActive;
    }

    public int getMaxParallelThreadCount()
    {
        return maxParallelThreadCount;
    }

    public void setMaxParallelThreadCount( int maxParallelThreadCount )
    {
        this.maxParallelThreadCount = maxParallelThreadCount;
    }
}
