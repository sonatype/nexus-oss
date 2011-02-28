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
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A shadow repository that transforms M2 layout of master to M1 layouted shadow.
 * 
 * @author cstamas
 */
@Component( role = ShadowRepository.class, hint = M1LayoutedM2ShadowRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2 to Maven1" )
public class M1LayoutedM2ShadowRepository
    extends LayoutConverterShadowRepository
{
    /** This "mimics" the @Named("m2-m1-shadow") */
    public static final String ID = "m2-m1-shadow";

    @Requirement( hint = Maven1ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( hint = Maven2ContentClass.ID )
    private ContentClass masterContentClass;

    @Requirement
    private M1LayoutedM2ShadowRepositoryConfigurator m1LayoutedM2ShadowRepositoryConfigurator;

    @Override
    public M1LayoutedM2ShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M1LayoutedM2ShadowRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<M1LayoutedM2ShadowRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M1LayoutedM2ShadowRepositoryConfiguration>()
        {
            public M1LayoutedM2ShadowRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M1LayoutedM2ShadowRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public GavCalculator getGavCalculator()
    {
        return getM1GavCalculator();
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return m1LayoutedM2ShadowRepositoryConfigurator;
    }

    protected String transformMaster2Shadow( String path )
    {
        return transformM2toM1( path );
    }

    protected String transformShadow2Master( String path )
    {
        return transformM1toM2( path );
    }

    public boolean isMavenMetadataPath( String path )
    {
        return M1ArtifactRecognizer.isMetadata( path );
    }
}
