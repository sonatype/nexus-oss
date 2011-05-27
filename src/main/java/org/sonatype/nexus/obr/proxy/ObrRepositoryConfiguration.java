/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.proxy;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

public class ObrRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
    private final static String OBR_PATH_KEY = "obrPath";

    public static final String METADATA_MAX_AGE_KEY = "metadataMaxAge";

    public ObrRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public boolean isObrPathSet()
    {
        return getRootNode().getChild( OBR_PATH_KEY ) != null;
}

    public String getObrPath()
    {
        return getNodeValue( getRootNode(), OBR_PATH_KEY, "/repository.xml" );
    }

    public void setObrPath( String val )
    {
        setNodeValue( getRootNode(), OBR_PATH_KEY, val );
    }

    public int getMetadataMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), METADATA_MAX_AGE_KEY, "1440" ) );
    }

    public void setMetadataMaxAge( int age )
    {
        setNodeValue( getRootNode(), METADATA_MAX_AGE_KEY, String.valueOf( age ) );
    }
}
