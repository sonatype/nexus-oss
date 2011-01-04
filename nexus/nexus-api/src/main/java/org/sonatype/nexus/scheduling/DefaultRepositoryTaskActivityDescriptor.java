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
package org.sonatype.nexus.scheduling;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultRepositoryTaskActivityDescriptor
    implements RepositoryTaskActivityDescriptor
{
    public static final Set<ModificationOperator> ALL_CONTENT_OPERATIONS = new HashSet<ModificationOperator>( 3 );
    {
        ALL_CONTENT_OPERATIONS.add( ModificationOperator.create );

        ALL_CONTENT_OPERATIONS.add( ModificationOperator.update );

        ALL_CONTENT_OPERATIONS.add( ModificationOperator.delete );

    }

    public static final Set<AttributesModificationOperator> ALL_ATTRIBUTES_OPERATIONS = new HashSet<AttributesModificationOperator>(
        3 );
    {
        ALL_ATTRIBUTES_OPERATIONS.add( AttributesModificationOperator.extend );

        ALL_ATTRIBUTES_OPERATIONS.add( AttributesModificationOperator.refresh );

        ALL_ATTRIBUTES_OPERATIONS.add( AttributesModificationOperator.lessen );

    }

    private boolean scheduled;

    private boolean userInitiated;

    private boolean scanningRepository;

    private String repositoryScanningStartingPath;

    private Set<ModificationOperator> contentModificationOperators;

    private Set<AttributesModificationOperator> attributesModificationOperators;

    public boolean isScheduled()
    {
        return scheduled;
    }

    public DefaultRepositoryTaskActivityDescriptor setScheduled( boolean scheduled )
    {
        this.scheduled = scheduled;

        return this;
    }

    public boolean isUserInitiated()
    {
        return userInitiated;
    }

    public DefaultRepositoryTaskActivityDescriptor setUserInitiated( boolean userInitiated )
    {
        this.userInitiated = userInitiated;

        return this;
    }

    public boolean isScanningRepository()
    {
        return scanningRepository;
    }

    public DefaultRepositoryTaskActivityDescriptor setScanningRepository( boolean scanningRepository )
    {
        this.scanningRepository = scanningRepository;

        return this;
    }

    public String getRepositoryScanningStartingPath()
    {
        return repositoryScanningStartingPath;
    }

    public DefaultRepositoryTaskActivityDescriptor setRepositoryScanningStartingPath(
        String repositoryScanningStartingPath )
    {
        setScanningRepository( repositoryScanningStartingPath != null );

        this.repositoryScanningStartingPath = repositoryScanningStartingPath;

        return this;
    }

    public Set<ModificationOperator> getContentModificationOperators()
    {
        return Collections.unmodifiableSet( getOrCreateContentModificationOperators() );
    }

    public DefaultRepositoryTaskActivityDescriptor addContentModificationOperators(
        ModificationOperator contentModificationOperator )
    {
        getOrCreateContentModificationOperators().add( contentModificationOperator );

        return this;
    }

    public DefaultRepositoryTaskActivityDescriptor setContentModificationOperators(
        Set<ModificationOperator> contentModificationOperators )
    {
        this.contentModificationOperators = contentModificationOperators;

        return this;
    }

    public Set<AttributesModificationOperator> getAttributesModificationOperators()
    {
        return Collections.unmodifiableSet( getOrCreateAttributesModificationOperators() );
    }

    public DefaultRepositoryTaskActivityDescriptor addAttributesModificationOperator(
        AttributesModificationOperator attributesModificationOperator )
    {
        getOrCreateAttributesModificationOperators().add( attributesModificationOperator );

        return this;
    }

    public DefaultRepositoryTaskActivityDescriptor setAttributesModificationOperators(
        Set<AttributesModificationOperator> attributesModificationOperators )
    {
        this.attributesModificationOperators = attributesModificationOperators;

        return this;
    }

    protected Set<ModificationOperator> getOrCreateContentModificationOperators()
    {
        if ( contentModificationOperators == null )
        {
            contentModificationOperators = new HashSet<ModificationOperator>();
        }
        return contentModificationOperators;
    }

    protected Set<AttributesModificationOperator> getOrCreateAttributesModificationOperators()
    {
        if ( attributesModificationOperators == null )
        {
            attributesModificationOperators = new HashSet<AttributesModificationOperator>();
        }
        return attributesModificationOperators;
    }

    public boolean allowedExecution( TaskFilter filter )
    {
        if ( filter.allowsScheduledTasks() != isScheduled() )
        {
            return false;
        }

        if ( filter.allowsUserInitiatedTasks() != isUserInitiated() )
        {
            return false;
        }

        if ( filter instanceof RepositoryTaskFilter )
        {
            RepositoryTaskFilter rfilter = (RepositoryTaskFilter) filter;

            if ( isScanningRepository() && !rfilter.allowsRepositoryScanning( getRepositoryScanningStartingPath() ) )
            {
                return false;
            }

            if ( !rfilter.allowsContentOperations( getContentModificationOperators() ) )
            {
                return false;
            }

            if ( !rfilter.allowsAttributeOperations( getAttributesModificationOperators() ) )
            {
                return false;
            }
        }

        // All tests passed, it is OK to run
        return true;
    }

}
