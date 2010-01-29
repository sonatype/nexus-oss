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
package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M1ArtifactRecognizer;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;

@Component( role = GroupRepository.class, hint = "maven1", instantiationStrategy = "per-lookup", description = "Maven1 Repository Group" )
public class M1GroupRepository
    extends AbstractMavenGroupRepository
{
    @Requirement( hint = "maven1" )
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
