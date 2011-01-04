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
package org.sonatype.nexus.proxy.maven.maven1;

import org.apache.maven.index.artifact.GavCalculator;
import org.apache.maven.index.artifact.M1ArtifactRecognizer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;

@Component( role = GroupRepository.class, hint = M1GroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven1 Repository Group" )
public class M1GroupRepository
    extends AbstractMavenGroupRepository
{
    /** This "mimics" the @Named("maven1") */
    public static final String ID = Maven1ContentClass.ID;
    
    @Requirement( hint = Maven1ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( hint = "maven1" )
    private GavCalculator gavCalculator;

    @Requirement
    private M1GroupRepositoryConfigurator m1GroupRepositoryConfigurator;

    @Override
    protected M1GroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M1GroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<M1GroupRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M1GroupRepositoryConfiguration>()
        {
            public M1GroupRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M1GroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return m1GroupRepositoryConfigurator;
    }
    
    public boolean isMavenMetadataPath( String path )
    {
        return M1ArtifactRecognizer.isMetadata( path );
    }
}
