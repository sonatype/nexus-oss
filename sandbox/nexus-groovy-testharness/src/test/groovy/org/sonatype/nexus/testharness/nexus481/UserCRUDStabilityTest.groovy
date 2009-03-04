package org.sonatype.nexus.testharness.nexus481

import static org.testng.Assert.*
import org.testng.annotations.Test
import org.testng.annotations.BeforeClass
import org.testng.annotations.AfterClass
import org.sonatype.nexus.util.MessageUtil
import org.sonatype.nexus.groovytest.NexusCompatibilityimport groovy.util.XmlSlurperimport groovy.xml.StreamingMarkupBuilderimport org.codehaus.plexus.component.annotations.Component;

@Component(role = UserCRUDStabilityTest.class)
class UserCRUDStabilityTest
{

     def static msgUtil;
     
     def static users = new Vector();

     @BeforeClass
     static void init() {
        msgUtil = new MessageUtil(messageUrl:"users/", messageIdUrl:"users/\$id");
     }

     @Test(threadPoolSize = 10, invocationCount = 10, timeOut = 2000L )
     @NexusCompatibility (minVersion = "1.3")
     void userCreationStability() {
        def mark = Long.toHexString( System.nanoTime() );
        users.add( "velo" + mark );
        
        def s_xml=new StringWriter()
        def builder=new groovy.xml.MarkupBuilder(s_xml);
        builder.'user-request'(){
            data(){
              userId( "velo" + mark )
              name("velo" + mark)
              email("velo@sonatype.org")
              status("active")
              password("velo" + mark)
              roles {
                role("admin")
              }
            }
          }

        def result = msgUtil.doPost( s_xml.toString() );
        assertEquals result.code, 201

        //read and check
        result = msgUtil.doGet( "velo" + mark );
        assertEquals result.code, 200
        
        assertEquals result.content.data.name.text(), "velo" + mark;
     }

     @AfterClass
     static void deleteUsers() {
//         def result = msgUtil.doGet();
//         result.content.data.'users-list-item'.each {
//             def id = it.userId.text();
//             if(id.startsWith("velo")) {
//                 println "Delete $id";
//                 def delResult = msgUtil.doDelete( id );
//                 assertEquals delResult.code, 204
//             }
//         }
         users.each{
             //println "Delete $it";
             if(it != null) {
                 def delResult = msgUtil.doDelete( it );
                 if( delResult.code !=  204 // was deleted 
                                 && delResult.code != 404) { // was not inserted
                     fail "Unable to delete user $it"; 
                 }
             }
         }
     }
}
