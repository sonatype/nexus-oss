package org.sonatype.nexus.plugins.yum.repository;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang.StringUtils.join;
import static org.freecompany.redline.header.Header.HeaderTag.ARCH;
import static org.freecompany.redline.header.Header.HeaderTag.NAME;
import static org.freecompany.redline.header.Header.HeaderTag.OS;
import static org.freecompany.redline.header.Header.HeaderTag.SOURCERPM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.Scanner;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Header.HeaderTag;
import org.freecompany.redline.payload.CpioHeader;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.yum.repository.RepositoryRpmGenerator;


public class RepositoryRpmGeneratorTest {
  private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
  private static final String BASE_URL = "http://blabla";
  private static final String VERSION = "1.0.0-SNAPSHOT";
  private static final String REPO_ID = "snapshots";
  private static final String REPO_NAME = format("is24-rel-%s-%s-repo", REPO_ID, VERSION.toLowerCase());
  private static final String REPO_FILENAME = format("./etc/yum.repos.d/is24-rel-%s-%s.repo", REPO_ID,
    VERSION.toLowerCase());
  private static final File OUTPUT_DIR = new File(".", "target/tmp/repo-rpm");
  private static final File TMP_DIR = new File(".", "target/tmp/repo-tmp");
  private static final File RPM_FILE = new File(OUTPUT_DIR, REPO_NAME + "-1-1.noarch.rpm");

  private String originalTmpDir;

  @Before
  public void initTempDir() throws IOException {
    deleteDirectory(OUTPUT_DIR);
    OUTPUT_DIR.mkdirs();
    deleteDirectory(TMP_DIR);
    TMP_DIR.mkdirs();
    originalTmpDir = System.getProperty(JAVA_IO_TMPDIR);
    System.setProperty(JAVA_IO_TMPDIR, TMP_DIR.getAbsolutePath());
  }

  public void resetTmpDir() {
    System.setProperty(JAVA_IO_TMPDIR, originalTmpDir);
  }

  @Test
  public void shouldGenerateRepoRpm() throws Exception {
    givenGeneratedRepoRpm();
    assertTrue("RPM does not exists", RPM_FILE.exists());
  }

  @Test
  public void shouldContainCorrectRpmHeader() throws Exception {
    givenGeneratedRepoRpm();
    thenAssertHeaderInfosAreCorrect();
  }

  @Test
  public void shouldGenerateCorrectRepoFileInsideRpm() throws Exception {
    givenGeneratedRepoRpm();
    thenAssertRepoContentIsCorrect();
  }

  @Test
  public void shouldDeleteRepoTmpFile() throws Exception {
    givenGeneratedRepoRpm();
    thenAssertTmpDirIsEmpty();
  }

  private void thenAssertTmpDirIsEmpty() {
    assertThat(TMP_DIR.list().length, is(0));
  }

  private void thenAssertRepoContentIsCorrect() throws FileNotFoundException, Exception, IOException {
    FileInputStream rpmInputStream = new FileInputStream(RPM_FILE);
    extractFormat(rpmInputStream);

    InputStream uncompressed = new GZIPInputStream(rpmInputStream);
    byte[] buffer = toByteArray(uncompressed);
    String content = new String(assertRpmContainsFile(buffer, REPO_FILENAME));
    assertThat(content, containsString(BASE_URL + "/service/local/yum/" + REPO_ID + "/" + VERSION));
  }

  private void thenAssertHeaderInfosAreCorrect() throws FileNotFoundException, Exception {
    FileInputStream rpmInputStream = new FileInputStream(RPM_FILE);
    Format format = extractFormat(rpmInputStream);
    assertHeader(format, NAME, REPO_NAME);
    assertHeader(format, ARCH, "noarch");
    assertHeader(format, OS, "linux");
    assertHeader(format, SOURCERPM, "dummy-source-rpm-because-yum-needs-this");
  }

  private void givenGeneratedRepoRpm() throws IOException, NoSuchAlgorithmException {
    new RepositoryRpmGenerator(BASE_URL, REPO_ID, VERSION, OUTPUT_DIR).generateReleaseRpm();
  }

  private byte[] assertRpmContainsFile(byte[] buffer, String path) throws IOException {
    InputStream inputStream = new ByteArrayInputStream(buffer);
    ReadableChannelWrapper in = new ReadableChannelWrapper(Channels.newChannel(inputStream));

    CpioHeader header;
    int total = 0;
    do {
      header = new CpioHeader();
      total = header.read(in, total);

      final int fileSize = header.getFileSize();
      byte[] fileBuf = new byte[fileSize];

      if (inputStream.read(fileBuf) != fileSize) {
        throw new RuntimeException("Reading file failed.");
      }

      if (header.getName().equals(path)) {
        return fileBuf;
      }
      total += header.getFileSize();
    } while (!header.isLast());

    throw new AssertionError("There's no header with name '" + path + "'.");
  }

  private void assertHeader(Format format, HeaderTag tag, String value) {
    Object[] values = (Object[]) format.getHeader().getEntry(tag).getValues();
    if (values == null) {
      throw new AssertionError("HeaderTag " + tag.getName() + " did not matched " + value + ", instead it was null");
    }
    assertEquals("HeaderTag " + tag.getName() + " did not matched " + value, value,
      join(values, ","));
  }

  private Format extractFormat(InputStream rpmInputStream) throws Exception {
    ReadableChannelWrapper channel = new ReadableChannelWrapper(Channels.newChannel(rpmInputStream));
    return new Scanner().run(channel);
  }

}
