/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus810;

import static org.hamcrest.CoreMatchers.*;
import static org.sonatype.nexus.test.utils.ResponseMatchers.*;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Checks to make sure the tasks don't have packages in the type field.
 */
public class Nexus810PackageNamesInRestMessagesIT
    extends AbstractNexusIntegrationTest
{
    @BeforeClass
    public void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void checkForPackageNamesInResponse()
        throws IOException
    {
        Matcher<Response> matcher = allOf( isSuccessful(), not( responseText( containsString( "org.sonatype." ) ) ) );
        RequestFacade.doGet( "service/local/schedule_types", matcher );
    }
}
