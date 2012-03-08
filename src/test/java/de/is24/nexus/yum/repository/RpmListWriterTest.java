package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.RPM_BASE_FILE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import de.is24.nexus.yum.repository.config.YumGeneratorConfiguration;


public class RpmListWriterTest {
  private static final String FILE_CONTENT = "another-artifact/0.0.1/another-artifact-0.0.1-1.noarch.rpm\n" +
    "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm\n" +
    "conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n" +
    "test-artifact/1.2/test-artifact-1.2-1.noarch.rpm\n" + "test-artifact/1.3/test-artifact-1.3-1.noarch.rpm\n";

  @Test
  public void shouldListFileInSubDirs() throws Exception {
    File rpmListFile = writeRpmListFile(config(RPM_BASE_FILE, null, null));
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldListPackagesOnHighestLevel() throws Exception {
    File rpmListFile = writeRpmListFile(config(new File(RPM_BASE_FILE, "conflict-artifact/2.2-2"), null, null));
    assertEquals("conflict-artifact-2.2-2.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldRemoveNotExistingRpmsAndAddNewlyAddedFile() throws Exception {
    File rpmListFile = writeRpmListFile(config(RPM_BASE_FILE, null, null));
    rpmListFile = new RpmListWriter(config(new File(RPM_BASE_FILE, "tomcat-mysql-jdbc/5.1.15-2"), null,
        "/is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm"), wrap(rpmListFile)).writeList();
    assertEquals("is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldReuseExistingPackageFile() throws Exception {
    YumGeneratorConfiguration config = config(RPM_BASE_FILE, null, null);
    File rpmListFile = writeRpmListFile(config);
    rpmListFile = new RpmListWriter(config, wrap(rpmListFile)).writeList();
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldCreateVersionSpecificRpmListFile() throws Exception {
    File rpmListFile = writeRpmListFile(config(RPM_BASE_FILE, "2.2-2", null));
    assertEquals("conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldNotAddDuplicateToList() throws Exception {
    YumGeneratorConfiguration config = config(RPM_BASE_FILE, null, null);
    File rpmListFile = writeRpmListFile(config);

    YumGeneratorConfiguration addingFileConfig = config(RPM_BASE_FILE, null,
      "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm");
    rpmListFile = new RpmListWriter(addingFileConfig, wrap(rpmListFile)).writeList();
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  private File writeRpmListFile(YumGeneratorConfiguration config) throws IOException {
    File rpmListFile = File.createTempFile("package.", ".txt");
    rpmListFile.delete();

    new RpmListWriter(config, wrap(rpmListFile)).writeList();
    return rpmListFile;
  }

  private YumGeneratorConfiguration config(File rpmBaseDir, String version, String addedFile) {
    YumGeneratorConfiguration config = createMock(YumGeneratorConfiguration.class);
    expect(config.getBaseRpmDir()).andReturn(rpmBaseDir).anyTimes();
    expect(config.getVersion()).andReturn(version).anyTimes();
    expect(config.getAddedFile()).andReturn(addedFile).anyTimes();
    expect(config.getId()).andReturn("repoId").anyTimes();
    expect(config.isSingleRpmPerDirectory()).andReturn(true).anyTimes();
    replay(config);
    return config;
  }

  private ListFileFactory wrap(final File file) {
    return new ListFileFactory() {
      public File getRpmListFile(String id) {
        return file;
      }

      public File getRpmListFile(String id, String version) {
        return file;
      }
    };
  }
}
