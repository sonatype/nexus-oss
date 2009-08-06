/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.maven.maven2;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A shadow repository that transforms M1 content hierarchy of master to M2 layouted shadow.
 * 
 * @author cstamas
 */
@Component( role = ShadowRepository.class, hint = "m1-m2-shadow", instantiationStrategy = "per-lookup", description = "Maven1 to Maven2" )
public class M2LayoutedM1ShadowRepository
    extends LayoutConverterShadowRepository
{
    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    @Requirement( hint = "maven1" )
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
        throws IllegalArtifactCoordinateException
    {
        return transformM1toM2( path );
    }

    protected String transformShadow2Master( String path )
        throws IllegalArtifactCoordinateException
    {
        return transformM2toM1( path );
    }

}
