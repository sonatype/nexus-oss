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
}
