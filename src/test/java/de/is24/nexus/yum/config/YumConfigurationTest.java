package de.is24.nexus.yum.config;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Thread.sleep;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.xml.sax.SAXException;

import de.is24.nexus.yum.AbstractYumNexusTestCase;
import de.is24.nexus.yum.config.DefaultYumConfiguration;
import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.config.domain.XmlYumConfiguration;
import de.is24.nexus.yum.version.alias.AliasNotFoundException;
import de.is24.nexus.yum.version.alias.domain.AliasMapping;


/**
 * Created by IntelliJ IDEA. User: MKrautz Date: 7/8/11 Time: 3:02 PM To change
 * this template use File | Settings | File Templates.
 */
public class YumConfigurationTest extends AbstractYumNexusTestCase {
  private static final String YUM_XML = "yum.xml";

  private static final String PRODUCTION_VERSION = "5.1.15-1";

  private static final String NEW_PRODUCTION_VERSION = "5.5.5";

  private static final String PRODUCTION = "production";

  private static final String NEW_ALIAS_VERSION = "aliasVersion";

  private static final String NEW_ALIAS = "writeAlias";

  private static final String NEW_REPO_NAME = "writeRepo";

  private static final String TRUNK_VERSION = "5.1.15-2";

  private static final String TRUNK = "trunk";

  private static final String MYREPO_ID = "releases";

  @Inject
  private YumConfiguration yumConfiguration;

  @Inject
  private NexusConfiguration nexusConfiguration;

  @Before
  public void loadYumConfig() {
    yumConfiguration.load();
  }

  @Test
  public void loadConfigFile() throws Exception {
    final XmlYumConfiguration expectedXmlConf = createXmlyumConfig();

    final XmlYumConfiguration configuration = yumConfiguration.getXmlYumConfiguration();

    Assert.assertEquals(expectedXmlConf, configuration);
  }

  @Test
  public void saveConfig() throws Exception {
    final String testConfFilename = "yumWriteTest.xml";
    yumConfiguration.setFilename(testConfFilename);

    final XmlYumConfiguration confToWrite = createXmlyumConfig();
    confToWrite.setRepositoryCreationTimeout(150);
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
    Assert.assertEquals(TRUNK_VERSION, version);
  }

  @Test
  public void overrideExisting() throws Exception {
    final String newVersion = "myNewVersion";
    yumConfiguration.setAlias(MYREPO_ID, TRUNK, newVersion);

    final String actual = yumConfiguration.getVersion(MYREPO_ID, TRUNK);
    Assert.assertEquals(newVersion, actual);
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
    Assert.assertEquals(version, actual);
  }

  @Test
  public void shouldUpdateConfigIfFileIsWritten() throws Exception {
    yumConfiguration.load();
    manipulateConfigFile();
    Assert.assertEquals(NEW_PRODUCTION_VERSION, yumConfiguration.getVersion(MYREPO_ID, PRODUCTION));
  }

  @Test
  public void shouldCreateConfigFileIfNotExists() throws Exception {
    File tmpDir = createTmpDir();
    assertThat(new File(tmpDir, YUM_XML).exists(), is(FALSE));

    DefaultYumConfiguration yumConfigurationHandler = new DefaultYumConfiguration();
    yumConfigurationHandler.setNexusConfiguration(createNexusConfig(tmpDir));
    yumConfigurationHandler.load();
    assertThat(new File(tmpDir, YUM_XML).exists(), is(TRUE));
  }

  private NexusConfiguration createNexusConfig(File tmpDir) {
    NexusConfiguration nexusConfig = createMock(NexusConfiguration.class);
    expect(nexusConfig.getConfigurationDirectory()).andReturn(tmpDir).anyTimes();
    replay(nexusConfig);
    return nexusConfig;
  }

  private File createTmpDir() {
    File tmpDir = new File(".", "target/tmp/" + randomAlphabetic(10));
    tmpDir.mkdirs();
    return tmpDir;
  }

  private void manipulateConfigFile() throws FileNotFoundException, IOException, InterruptedException {
    // wait one second, because last modification date of files has granularity
    // 1 second.
    sleep(1000);

    String configContent;
    InputStream inputStream = new FileInputStream(yumConfiguration.getConfigFile());
    try {
      configContent = IOUtils.toString(inputStream);
    } finally {
      inputStream.close();
    }

    OutputStream outputStream = new FileOutputStream(yumConfiguration.getConfigFile());
    try {
      write(configContent.replace(PRODUCTION_VERSION, NEW_PRODUCTION_VERSION), outputStream);
    } finally {
      outputStream.close();
    }
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
