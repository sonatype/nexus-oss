package de.is24.nexus.yum.service.impl;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.xml.sax.SAXException;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.service.AliasNotFoundException;


/**
 * Created by IntelliJ IDEA. User: MKrautz Date: 7/8/11 Time: 3:02 PM To change
 * this template use File | Settings | File Templates.
 */
@RunWith(NexusTestRunner.class)
public class YumConfigurationTest {
  private static final int NEW_TIMEOUT = 5555;

  private static final String PRODUCTION_VERSION = "5.1.15-1";

  private static final String PRODUCTION = "production";

  private static final String NEW_ALIAS_VERSION = "aliasVersion";

  private static final String NEW_ALIAS = "writeAlias";

  private static final String NEW_REPO_NAME = "writeRepo";

  private static final String TRUNK_VERSION = "5.1.15-2";

  private static final String TRUNK = "trunk";

  private static final String MYREPO_ID = "releases";

  @Inject
  private YumConfigurationHandler yumConfiguration;

  @Inject
  private NexusConfiguration nexusConfiguration;

  @Test
  public void loadConfigFile() throws Exception {
    final XmlYumConfiguration expectedXmlConf = createXmlyumConfig();

    final XmlYumConfiguration configuration = yumConfiguration.getXmlYumConfiguration();

    assertEquals(expectedXmlConf, configuration);
  }

  @Test
  public void saveConfig() throws Exception {
    final String testConfFilename = "yumWriteTest.xml";
    yumConfiguration.setFilename(testConfFilename);

    final XmlYumConfiguration confToWrite = createXmlyumConfig();
    confToWrite.setRepositoryCreationTimeout(90);
    confToWrite.getAliasMappings().add(new AliasMapping(NEW_REPO_NAME, NEW_ALIAS, NEW_ALIAS_VERSION));

    yumConfiguration.saveConfig(confToWrite);

    assertSame(confToWrite, yumConfiguration.getXmlYumConfiguration());
    assertConfigSaved(testConfFilename);
  }

  @Test(expected = AliasNotFoundException.class)
  public void aliasMappingNotFound() throws Exception {
    yumConfiguration.getVersion("not", "present");
  }

  @Test
  public void loadedVersionFound() throws Exception {
    final String version = yumConfiguration.getVersion(MYREPO_ID, TRUNK);
    assertEquals(TRUNK_VERSION, version);
  }

  @Test
  public void overrideExisting() throws Exception {
    final String newVersion = "myNewVersion";
    yumConfiguration.setAlias(MYREPO_ID, TRUNK, newVersion);

    final String actual = yumConfiguration.getVersion(MYREPO_ID, TRUNK);
    assertEquals(newVersion, actual);
  }

  @Test
  public void newVersionSaved() throws Exception {
    final String testConfFilename = "yumWriteTest2.xml";
    yumConfiguration.setFilename(testConfFilename);
    yumConfiguration.setAlias(NEW_REPO_NAME, NEW_ALIAS, NEW_ALIAS_VERSION);
    assertConfigSaved(testConfFilename);
  }

  @Test
  public void newVersionFound() throws Exception {
    final String newRepo = "the new on";
    final String newAlias = "new alias";
    final String version = "the version";
    yumConfiguration.setAlias(newRepo, newAlias, version);

    final String actual = yumConfiguration.getVersion(newRepo, newAlias);
    assertEquals(version, actual);
  }

  @Test
  public void saveTimout() throws Exception {
    yumConfiguration.setRepositoryCreationTimeout(NEW_TIMEOUT);
    yumConfiguration.load();
    assertThat(yumConfiguration.getRepositoryCreationTimeout(), is(NEW_TIMEOUT));
  }

  private XmlYumConfiguration createXmlyumConfig() {
    final XmlYumConfiguration expectedXmlConf = new XmlYumConfiguration();
    expectedXmlConf.setRepositoryCreationTimeout(150);
    expectedXmlConf.setAliasMappings(new LinkedHashSet<AliasMapping>());
    expectedXmlConf.getAliasMappings().add(new AliasMapping(MYREPO_ID, TRUNK, TRUNK_VERSION));
    expectedXmlConf.getAliasMappings().add(new AliasMapping(MYREPO_ID, PRODUCTION, PRODUCTION_VERSION));
    return expectedXmlConf;
  }

  private void assertConfigSaved(final String testConfFilename) throws FileNotFoundException, SAXException,
    IOException {
    final FileReader expectedFile = new FileReader(new File(nexusConfiguration.getConfigurationDirectory(),
        "expetedWrittenYum.xml"));
    final FileReader writtenFile = new FileReader(new File(nexusConfiguration.getConfigurationDirectory(),
        testConfFilename));
    assertXMLEqual(expectedFile, writtenFile);
  }

}
