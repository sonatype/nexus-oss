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

    public static final String ENFORCE_RELEASE_REDOWNLOAD_POLICY = "enforceReleaseRedownloadPolicy";

    public AbstractMavenRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return RepositoryPolicy.valueOf( getNodeValue( getRootNode(), REPOSITORY_POLICY,
                                                       RepositoryPolicy.RELEASE.toString() ).toUpperCase() );
    }

    public void setRepositoryPolicy( RepositoryPolicy policy )
    {
        setNodeValue( getRootNode(), REPOSITORY_POLICY, policy.toString() );
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return ChecksumPolicy.valueOf( getNodeValue( getRootNode(), CHECKSUM_POLICY, ChecksumPolicy.WARN.toString() ) );
    }

    public void setChecksumPolicy( ChecksumPolicy policy )
    {
        setNodeValue( getRootNode(), CHECKSUM_POLICY, policy.toString() );
    }

    public int getArtifactMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), ARTIFACT_MAX_AGE, "1440" ) );
    }

    public void setArtifactMaxAge( int age )
    {
        setNodeValue( getRootNode(), ARTIFACT_MAX_AGE, String.valueOf( age ) );
    }

    public int getMetadataMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), METADATA_MAX_AGE, "1440" ) );
    }

    public void setMetadataMaxAge( int age )
    {
        setNodeValue( getRootNode(), METADATA_MAX_AGE, String.valueOf( age ) );
    }

    public boolean isDownloadRemoteIndex()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), DOWNLOAD_REMOTE_INDEX, Boolean.TRUE.toString() ) );
    }

    public void setDownloadRemoteIndex( boolean val )
    {
        setNodeValue( getRootNode(), DOWNLOAD_REMOTE_INDEX, Boolean.toString( val ) );
    }

    public boolean isCleanseRepositoryMetadata()
    {
        return Boolean
            .parseBoolean( getNodeValue( getRootNode(), CLEANSE_REPOSITORY_METADATA, Boolean.FALSE.toString() ) );
    }

    public void setCleanseRepositoryMetadata( boolean val )
    {
        setNodeValue( getRootNode(), CLEANSE_REPOSITORY_METADATA, Boolean.toString( val ) );
    }

    public boolean isEnforceReleaseRedownloadPolicy()
    {
        return Boolean
            .parseBoolean( getNodeValue( getRootNode(), ENFORCE_RELEASE_REDOWNLOAD_POLICY, Boolean.TRUE.toString() ) );
    }

    public void setEnforceReleaseRedownloadPolicy( boolean val )
    {
        setNodeValue( getRootNode(), ENFORCE_RELEASE_REDOWNLOAD_POLICY, Boolean.toString( val ) );
    }
}
