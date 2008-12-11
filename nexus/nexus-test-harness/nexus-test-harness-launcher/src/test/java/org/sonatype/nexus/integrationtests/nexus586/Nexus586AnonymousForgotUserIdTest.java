/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus586;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus395.ForgotUsernameUtils;

/**
 * Saving the Nexus config needs to validate the anonymous user information 
 */
public class Nexus586AnonymousForgotUserIdTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void forgotUsername()
        throws Exception
    {
        if( printKnownErrorButDoNotFail( Nexus586AnonymousResetPasswordTest.class, "forgotUsername" ))
        {
            return;
        }
         Status status = ForgotUsernameUtils.recoverUsername( "changeme2@yourcompany.com" );
         Assert.assertEquals( 400, status.getCode() );
    }
}
