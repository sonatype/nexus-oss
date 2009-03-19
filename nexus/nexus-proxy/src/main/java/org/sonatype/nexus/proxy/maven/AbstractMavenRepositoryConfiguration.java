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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

public abstract class AbstractMavenRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
    public static final String REPOSITORY_POLICY = "repositoryPolicy";

    public static final String CHECKSUM_POLICY = "checksumPolicy";

    public static final String ARTIFACT_MAX_AGE = "artifactMaxAge";

    public static final String METADATA_MAX_AGE = "metadataMaxAge";

    public static final String DOWNLOAD_REMOTE_INDEX = "downloadRemoteIndex";

    public static final String CLEANSE_REPOSITORY_METADATA = "cleanseRepositoryMetadata";

    public AbstractMavenRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return RepositoryPolicy.valueOf( getNodeValue(
            getConfiguration( false ),
            REPOSITORY_POLICY,
            RepositoryPolicy.RELEASE.toString() ) );
    }

    public void setRepositoryPolicy( RepositoryPolicy policy )
    {
        setNodeValue( getConfiguration( true ), REPOSITORY_POLICY, policy.toString() );
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return ChecksumPolicy.valueOf( getNodeValue( getConfiguration( false ), CHECKSUM_POLICY, ChecksumPolicy.WARN
            .toString() ) );
    }

    public void setChecksumPolicy( ChecksumPolicy policy )
    {
        setNodeValue( getConfiguration( true ), CHECKSUM_POLICY, policy.toString() );
    }

    public int getArtifactMaxAge()
    {
        return Integer.parseInt( getNodeValue( getConfiguration( false ), ARTIFACT_MAX_AGE, "1440" ) );
    }

    public void setArtifactMaxAge( int age )
    {
        setNodeValue( getConfiguration( true ), ARTIFACT_MAX_AGE, String.valueOf( age ) );
    }

    public int getMetadataMaxAge()
    {
        return Integer.parseInt( getNodeValue( getConfiguration( false ), METADATA_MAX_AGE, "1440" ) );
    }

    public void setMetadataMaxAge( int age )
    {
        setNodeValue( getConfiguration( true ), METADATA_MAX_AGE, String.valueOf( age ) );
    }

    public boolean isDownloadRemoteIndex()
    {
        return Boolean.parseBoolean( getNodeValue( getConfiguration( false ), DOWNLOAD_REMOTE_INDEX, Boolean.TRUE
            .toString() ) );
    }

    public void setDownloadRemoteIndex( boolean val )
    {
        setNodeValue( getConfiguration( true ), DOWNLOAD_REMOTE_INDEX, Boolean.toString( val ) );
    }

    public boolean isCleanseRepositoryMetadata()
    {
        return Boolean.parseBoolean( getNodeValue(
            getConfiguration( false ),
            CLEANSE_REPOSITORY_METADATA,
            Boolean.FALSE.toString() ) );
    }

    public void setCleanseRepositoryMetadata( boolean val )
    {
        setNodeValue( getConfiguration( true ), CLEANSE_REPOSITORY_METADATA, Boolean.toString( val ) );
    }
}
