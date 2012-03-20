package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.RPM_BASE_FILE;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class RpmListWriterTest {
  private static final String REPO_ID = "repoId";
  private static final String FILE_CONTENT = "another-artifact/0.0.1/another-artifact-0.0.1-1.noarch.rpm\n" +
    "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm\n" +
    "conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n" +
    "test-artifact/1.2/test-artifact-1.2-1.noarch.rpm\n" + "test-artifact/1.3/test-artifact-1.3-1.noarch.rpm\n";

  @Test
  public void shouldListFileInSubDirs() throws Exception {
    File rpmListFile = writeRpmListFile(RPM_BASE_FILE, null, null);
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldListPackagesOnHighestLevel() throws Exception {
    File rpmListFile = writeRpmListFile(new File(RPM_BASE_FILE, "conflict-artifact/2.2-2"), null, null);
    assertEquals("conflict-artifact-2.2-2.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldRemoveNotExistingRpmsAndAddNewlyAddedFile() throws Exception {
    File rpmListFile = writeRpmListFile(RPM_BASE_FILE, null, null);
    rpmListFile = new RpmListWriter(REPO_ID, new File(RPM_BASE_FILE, "tomcat-mysql-jdbc/5.1.15-2").getAbsolutePath(),
        "/is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm", null, true, wrap(rpmListFile)).writeList();
    assertEquals("is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldReuseExistingPackageFile() throws Exception {
    File rpmListFile = writeRpmListFile(RPM_BASE_FILE, null, null);
    rpmListFile = new RpmListWriter(REPO_ID, RPM_BASE_FILE.getAbsolutePath(), null, null, true, wrap(rpmListFile)).writeList();
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldCreateVersionSpecificRpmListFile() throws Exception {
    File rpmListFile = writeRpmListFile(RPM_BASE_FILE, "2.2-2", null);
    assertEquals("conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n",
      IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  @Test
  public void shouldNotAddDuplicateToList() throws Exception {
    File rpmListFile = writeRpmListFile(RPM_BASE_FILE, null, null);
    rpmListFile = new RpmListWriter(REPO_ID, RPM_BASE_FILE.getAbsolutePath(), "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm",
        null, true, wrap(rpmListFile)).writeList();
    assertEquals(FILE_CONTENT, IOUtils.toString(new FileInputStream(rpmListFile)));
  }

  private File writeRpmListFile(File rpmBaseDir, String version, String addedFile) throws IOException {
    File rpmListFile = File.createTempFile("package.", ".txt");
    rpmListFile.delete();

    new RpmListWriter(REPO_ID, rpmBaseDir.getAbsolutePath(), addedFile, version, true, wrap(rpmListFile)).writeList();
    return rpmListFile;
  }

  private ListFileFactory wrap(final File file) {
    return new ListFileFactory() {
      @Override
      public File getRpmListFile(String id) {
        return file;
      }

      @Override
      public File getRpmListFile(String id, String version) {
        return file;
      }
    };
  }
}
