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
package org.sonatype.nexus.bundle.launcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.runners.Parameterized.Parameters;
import static org.sonatype.nexus.bundle.launcher.ParametersLoader.loadDefaultTestParameters;
import static org.sonatype.nexus.bundle.launcher.ParametersLoader.loadTestParameters;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.URLMatchers.respondsWithStatus;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 * Test starting and launching of Nexus using a test specific filtered parameters file.
 *
 * @since 2.2
 */
public class StartAndStopNexusParametrizedFilteredIT
    extends StartAndStopNexusParametrizedIT
{

    @Parameters
    public static Collection<Object[]> data()
    {
        return loadTestParameters( StartAndStopNexusParametrizedFilteredIT.class );
    }

    public StartAndStopNexusParametrizedFilteredIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

}
