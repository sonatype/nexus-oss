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
package org.sonatype.nexus.integrationtests.nexus156;

import java.io.IOException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.test.utils.RoleMessageUtil;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus156RolesCrudXmlIT
    extends Nexus156RolesCrudJsonIT
{

    public Nexus156RolesCrudXmlIT()
    {
        this.messageUtil = new RoleMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void readTest()
        throws IOException
    {
        super.readTest();
    }
    
}
