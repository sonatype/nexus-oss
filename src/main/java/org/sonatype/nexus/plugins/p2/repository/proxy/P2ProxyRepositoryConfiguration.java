/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.proxy;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

public class P2ProxyRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
    public static final String ARTIFACT_MAX_AGE = "artifactMaxAge";

    public static final String METADATA_MAX_AGE = "metadataMaxAge";

    public static final String CHECKSUM_POLICY = "checksumPolicy";

    public P2ProxyRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
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

    public ChecksumPolicy getChecksumPolicy()
    {
        return ChecksumPolicy.valueOf( getNodeValue( getRootNode(), CHECKSUM_POLICY,
                                                     ChecksumPolicy.STRICT_IF_EXISTS.toString() ) );
    }

    public void setChecksumPolicy( ChecksumPolicy policy )
    {
        setNodeValue( getRootNode(), CHECKSUM_POLICY, policy.toString() );
    }

}
