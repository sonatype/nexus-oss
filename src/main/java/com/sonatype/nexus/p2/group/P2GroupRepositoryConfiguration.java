/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.group;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfiguration;

public class P2GroupRepositoryConfiguration
    extends AbstractGroupRepositoryConfiguration
{
    public P2GroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }
}
