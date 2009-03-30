/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.DefaultWagonManager.WagonConfigurationException;

/**
 * Manages <a href="http://maven.apache.org/wagon">Wagon</a> related operations in Maven.
 * 
 * @author <a href="michal.maczka@dimatics.com">Michal Maczka </a>
 */
public interface WagonManager
{
    /**
     * Get a Wagon provider that understands the protocol passed as argument. It doesn't configure the Wagon.
     * 
     * @param protocol the protocol the {@link Wagon} will handle
     * @return the {@link Wagon} instance able to handle the protocol provided
     * @throws UnsupportedProtocolException if there is no provider able to handle the protocol
     * @deprecated prone to errors. use {@link #getWagon(Repository)} instead.
     */
    Wagon getWagon( String protocol )
        throws WagonException;

    /**
     * Get a Wagon provider for the provided repository. It will configure the Wagon for that repository.
     * 
     * @param repository the repository
     * @return the {@link Wagon} instance that can be used to connect to the repository
     * @throws UnsupportedProtocolException if there is no provider able to handle the protocol
     * @throws WagonConfigurationException if the wagon can't be configured for the repository
     */
    Wagon getWagon( Repository repository )
        throws WagonException;

    void setInteractive( boolean interactive );

    /**
     * Set the configuration for a repository
     * 
     * @param repositoryId id of the repository to set the configuration to
     * @param configuration dom tree of the xml with the configuration for the {@link Wagon}
     */
    void addConfiguration( String repositoryId, Xpp3Dom configuration );
}
