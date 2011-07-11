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

    public P2ProxyRepositoryConfiguration( final Xpp3Dom configuration )
    {
        super( configuration );
    }

    public int getArtifactMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), ARTIFACT_MAX_AGE, "1440" ) );
    }

    public void setArtifactMaxAge( final int age )
    {
        setNodeValue( getRootNode(), ARTIFACT_MAX_AGE, String.valueOf( age ) );
    }

    public int getMetadataMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), METADATA_MAX_AGE, "1440" ) );
    }

    public void setMetadataMaxAge( final int age )
    {
        setNodeValue( getRootNode(), METADATA_MAX_AGE, String.valueOf( age ) );
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return ChecksumPolicy.valueOf( getNodeValue( getRootNode(), CHECKSUM_POLICY,
            ChecksumPolicy.STRICT_IF_EXISTS.toString() ) );
    }

    public void setChecksumPolicy( final ChecksumPolicy policy )
    {
        setNodeValue( getRootNode(), CHECKSUM_POLICY, policy.toString() );
    }

}
