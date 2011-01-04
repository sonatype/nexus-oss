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
package org.sonatype.nexus.testharness.nexus1747

import static org.testng.Assert.*
import org.testng.annotations.Test
import org.testng.annotations.BeforeClass
import org.sonatype.nexus.testharness.util.MessageUtil
import org.sonatype.nexus.groovytest.NexusCompatibilityimport groovy.util.XmlSlurperimport groovy.xml.StreamingMarkupBuilderimport org.codehaus.plexus.component.annotations.Component;

@Component(role = UserCRUDTest.class)
class UserCRUDTest
{

     def static msgUtil;

     @BeforeClass
     static void init() {
        msgUtil = new MessageUtil(messageUrl:"users/", messageIdUrl:"users/\$id");
     }

     @Test
     @NexusCompatibility (minVersion = "1.3")
     void createUser() {
        def s_xml=new StringWriter()
        def builder=new groovy.xml.MarkupBuilder(s_xml);
        builder.'user-request'(){
            data(){
              userId("velo")
              name("velo")
              email("velo@sonatype.org")
              status("active")
              password("velo")
              roles {
                role("admin")
              }
            }
          }

        def result = msgUtil.doPost( s_xml.toString() );
        assertEquals result.code, 201

        //read and check
        result = msgUtil.doGet("velo");
        assertEquals result.code, 200
        
        assertEquals result.content.data.name.text(), "velo";
     }

     @Test(dependsOnMethods = [ "createUser" ])
     @NexusCompatibility (minVersion = "1.3")
     void readUser() {
        def result = msgUtil.doGet("velo");
        assertEquals result.code, 200
        
        def user = result.content.data;
        assertEquals user.userId.text(), "velo";
        assertEquals user.name.text(), "velo";
        assertEquals user.email.text(), "velo@sonatype.org";
        assertEquals user.status.text(), "active";
        assertEquals user.roles.size(), 1;
        assertEquals user.roles[0].role.text(), "admin";
     }

     @Test(dependsOnMethods = [ "createUser", "readUser" ])
     @NexusCompatibility (minVersion = "1.3")
     void updateUser() {
        def result = msgUtil.doGet("velo");
        assertEquals result.code, 200
        
        def root = result.content;

        root.data.email = "marvin@sonatype.org"
        def outputBuilder = new StreamingMarkupBuilder()
        def xml = outputBuilder.bind{ mkp.yield root }
        
        result = msgUtil.doPut("velo", xml);
        assertEquals result.code, 200

        //read and check
        result = msgUtil.doGet("velo");
        assertEquals result.code, 200
        
        assertEquals result.content.data.email.text(), "marvin@sonatype.org";
     }

     @Test(dependsOnMethods = [ "createUser", "readUser", "updateUser" ], alwaysRun = true)
     @NexusCompatibility (minVersion = "1.3")
     void deleteUser() {
        def result = msgUtil.doDelete("velo");
        assertEquals result.code, 204
     }

}
