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
package org.sonatype.nexus.configuration.application;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public class DefaultGlobalRestApiConfigurationTest
    extends AbstractNexusTestCase
{

    @Test
    public void testNoConfiguration()
        throws ConfigurationException
    {
        DefaultGlobalRestApiSettings settings = new DefaultGlobalRestApiSettings();
        SimpleApplicationConfiguration cfg = new SimpleApplicationConfiguration();
        cfg.getConfigurationModel().setRestApi( null );
        settings.configure( cfg );

        Assert.assertNull( settings.getBaseUrl() );
        Assert.assertEquals( 0, settings.getUITimeout() );

        settings.setUITimeout( 1000 );
        settings.setBaseUrl( "http://invalid.url" );
        Assert.assertTrue( settings.commitChanges() );

        Assert.assertEquals( "http://invalid.url", settings.getBaseUrl() );
        Assert.assertEquals( 1000, settings.getUITimeout() );
    }

}
