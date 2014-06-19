package org.sonatype.nexus.db;

import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import org.junit.Test;

/**
 * Trials of using OrientDB.
 */
public class OrientDbTrial
  extends TestSupport
{
  @Test
  public void embeddedServerTest() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
    server.activate();
    server.shutdown();
  }
}
