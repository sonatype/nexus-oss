/*
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
package org.sonatype.nexus.rest.global;

import static org.sonatype.nexus.rest.global.SmtpSettingsValidationPlexusResource.validateEmail;

import org.junit.Test;
import org.restlet.resource.ResourceException;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

public class SmtpSettingsValidationPlexusResourceTest
    extends TestSupport
{

    @Test( expected = ResourceException.class )
    public void nullEmailShouldNotBeAccepted()
        throws ResourceException
    {
        validateEmail( null );
    }

    @Test( expected = ResourceException.class )
    public void emptyEmailShouldNotBeAccepted()
        throws ResourceException
    {
        validateEmail( " " );
    }

    @Test
    public void tldWithUpperCase()
        throws ResourceException
    {
        validateEmail( "me@foo.COM" );
    }

    @Test
    public void tldWithLowerCase()
        throws ResourceException
    {
        validateEmail( "me@foo.com" );
    }

    @Test
    public void tldWithMixCase()
        throws ResourceException
    {
        validateEmail( "me@foo.CoM" );
    }

}
