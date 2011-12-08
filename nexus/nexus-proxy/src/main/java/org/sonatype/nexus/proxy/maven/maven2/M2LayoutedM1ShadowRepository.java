/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.maven.maven2;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A shadow repository that transforms M1 content hierarchy of master to M2 layouted shadow.
 * 
 * @author cstamas
 */
@Component( role = ShadowRepository.class, hint = M2LayoutedM1ShadowRepository.ID, instantiationStrategy = "per-lookup", description = "Maven1 to Maven2" )
public class M2LayoutedM1ShadowRepository
    extends LayoutConverterShadowRepository
{
    /** This "mimics" the @Named("m1-m2-shadow") */
    public static final String ID = "m1-m2-shadow";

    @Requirement( hint = Maven2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( hint = Maven1ContentClass.ID )
    private ContentClass masterContentClass;

    @Requirement
    private M2LayoutedM1ShadowRepositoryConfigurator m2LayoutedM1ShadowRepositoryConfigurator;

    @Override
    protected M2LayoutedM1ShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M2LayoutedM1ShadowRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M2LayoutedM1ShadowRepositoryConfiguration>()
        {
            public M2LayoutedM1ShadowRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M2LayoutedM1ShadowRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public GavCalculator getGavCalculator()
    {
        return getM2GavCalculator();
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
        return m2LayoutedM1ShadowRepositoryConfigurator;
    }

    protected String transformMaster2Shadow( String path )
    {
        return transformM1toM2( path );
    }

    protected String transformShadow2Master( String path )
    {
        return transformM2toM1( path );
    }

    public boolean isMavenMetadataPath( String path )
    {
        return M2ArtifactRecognizer.isMetadata( path );
    }

}
