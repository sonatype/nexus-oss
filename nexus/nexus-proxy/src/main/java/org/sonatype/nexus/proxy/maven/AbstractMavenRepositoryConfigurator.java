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
package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.modello.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;

public class AbstractMavenRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{
    public static final String REPOSITORY_POLICY = "repositoryPolicy";

    public static final String CHECKSUM_POLICY = "checksumPolicy";

    public static final String ARTIFACT_MAX_AGE = "artifactMaxAge";

    public static final String METADATA_MAX_AGE = "metadataMaxAge";

    public static final String MAINTAIN_PROXIED_REPOSITORY_METADATA = "maintainProxiedRepositoryMetadata";

    @Override
    public void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        super.doConfigure( repository, configuration, repo, externalConfiguration );

        MavenRepository mrepository = repository.adaptToFacet( MavenRepository.class );

        try
        {
            mrepository.setRepositoryPolicy( RepositoryPolicy.valueOf( externalConfiguration.getChild(
                REPOSITORY_POLICY ).getValue() ) );
        }
        catch ( PlexusConfigurationException e )
        {
            throw new InvalidConfigurationException( "Cannot read the repository policy for repository ID='"
                + repository.getId() + "'" );
        }

        if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            MavenProxyRepository mpr = repository.adaptToFacet( MavenProxyRepository.class );

            mpr.setChecksumPolicy( ChecksumPolicy.valueOf( externalConfiguration
                .getChild( CHECKSUM_POLICY ).getValue( ChecksumPolicy.WARN.toString() ) ) );

            mpr.setReleaseMaxAge( Integer.parseInt( externalConfiguration.getChild( ARTIFACT_MAX_AGE ).getValue(
                String.valueOf( 1440 ) ) ) );

            mpr.setSnapshotMaxAge( Integer.parseInt( externalConfiguration.getChild( ARTIFACT_MAX_AGE ).getValue(
                String.valueOf( 1440 ) ) ) );

            mpr.setMetadataMaxAge( Integer.parseInt( externalConfiguration.getChild( METADATA_MAX_AGE ).getValue(
                String.valueOf( 1440 ) ) ) );

            mpr.setCleanseRepositoryMetadata( Boolean.parseBoolean( externalConfiguration.getChild(
                MAINTAIN_PROXIED_REPOSITORY_METADATA ).getValue( String.valueOf( false ) ) ) );

        }
    }
}
