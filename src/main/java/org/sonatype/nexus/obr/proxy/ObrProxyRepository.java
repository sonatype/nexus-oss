/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.proxy;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

public interface ObrProxyRepository
    extends ProxyRepository
{
    /**
     * Returns the path <b>within OBR repository</b> for OBR metadata. Concatenating remoteUrl with this gives you full
     * URL of the OBR metadata.
     * 
     * @return
     */
    String getObrPath();

    /**
     * Sets the OBR metadata path.
     * 
     * @param obrPath
     */
    void setObrPath( String obrPath );

    /**
     * Returns the max age of the OBR metadata. Default is 1440 (1 day).
     * 
     * @return
     */
    int getMetadataMaxAge();

    /**
     * Sets the max age of the OBR metadata in minutes. Default is 1440 (1 day).
     * 
     * @param metadataMaxAge
     */
    void setMetadataMaxAge( int metadataMaxAge );
}
