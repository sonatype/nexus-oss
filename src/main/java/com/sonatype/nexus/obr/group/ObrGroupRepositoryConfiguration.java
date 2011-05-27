/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.group;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfiguration;

public class ObrGroupRepositoryConfiguration
    extends AbstractGroupRepositoryConfiguration
{
    public ObrGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
