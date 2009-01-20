/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus1071;

import org.junit.After;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

public class AbstractAnonAccessTest
    extends AbstractMavenNexusIT
{
    @Override
    public void oncePerClassSetUp()
        throws Exception
    {
        // this starts nexus with security enabled
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        super.oncePerClassSetUp();

        // this tells test harness NOT to login into nexus 
        TestContainer.getInstance().getTestContext().setSecureTest( false );
    }

    @After
    public void reenableSecurity()
    {
        // IT won't be able to shutdown nexus if security is disabled
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

}
