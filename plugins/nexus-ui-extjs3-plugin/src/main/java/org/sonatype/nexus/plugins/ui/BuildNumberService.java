/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ui;

import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class BuildNumberService
{

    private static final Logger logger = LoggerFactory.getLogger( BuildNumberService.class );

    private final String buildNumber;

    @Inject
    BuildNumberService()
    {
        Properties props = new Properties();

        InputStream is = getClass().getResourceAsStream( "version.properties" );
        try
        {
            props.load( is );
        }
        catch ( Exception e )
        {
            logger.warn( "Could not determine build qualifier", e );
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }

        buildNumber = props.getProperty( "version", "unknown-version" );
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

}
