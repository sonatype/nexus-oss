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
package org.sonatype.nexus.testng;

import java.util.Map.Entry;

import org.codehaus.plexus.ContainerConfiguration;
import org.sonatype.appbooter.AbstractPlexusAppBooterCustomizer;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.test.utils.TestProperties;

public class SeleniumAppBooterCustomizer
    extends AbstractPlexusAppBooterCustomizer
{

    @Override
    public void customizeContext( final PlexusAppBooter appBooter, final AppContext ctx )
    {
        for ( Entry<String, String> entry : TestProperties.getAll().entrySet() )
        {
            ctx.put( entry.getKey(), entry.getValue() );
        }
        super.customizeContext( appBooter, ctx );
    }

    @Override
    public void customizeContainerConfiguration( final PlexusAppBooter appBooter, final ContainerConfiguration config )
    {
        config.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );

        super.customizeContainerConfiguration( appBooter, config );
    }
}
