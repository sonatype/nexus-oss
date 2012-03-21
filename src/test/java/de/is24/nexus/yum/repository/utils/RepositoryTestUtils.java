package de.is24.nexus.yum.repository.utils;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.custommonkey.xmlunit.Diff;
import org.freecompany.redline.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.is24.nexus.yum.repository.xml.TimeStampIgnoringDifferenceListener;


public final class RepositoryTestUtils {
  public static final File BASE_TMP_FILE = new File("target/test-tmp");
  public static final File BASE_FILE = new File("target/test-classes");
  public static final File RPM_BASE_FILE = new File(BASE_FILE, "repo");
  public static final File TARGET_DIR = new File(BASE_TMP_FILE, "generated-repos");
  public static final File BASE_CACHE_DIR = new File(BASE_TMP_FILE, ".cache");
  public static final File PACKAGE_CACHE_DIR = new File(BASE_CACHE_DIR, ".packageFiles");
  public static final File REPOSITORY_RPM_CACHE_DIR = new File(BASE_CACHE_DIR, "yum/.repositoryRpms");
  public static final File REPODATA_DIR = new File(TARGET_DIR, "repodata");
  public static final File TEMPLATE_DIR = new File(BASE_FILE, "templates");
  public static final String PRIMARY_XML = "primary.xml";
  public static final String REPOMD_XML = "repomd.xml";
  public static final String PRIMARY_XML_GZ = PRIMARY_XML + ".gz";

  private static final Logger log = LoggerFactory.getLogger(RepositoryTestUtils.class);

  public static void assertRepository(File repodataDir, String templateName) throws Exception {
    log.info("Testing Repo {} ...", repodataDir);
    assertTrue(repodataDir.exists());
    assertRepomdXml(repodataDir, templateName);
    assertPrimaryXml(repodataDir, templateName);
  }

  private static void assertPrimaryXml(File repodataDir, String templateName) throws Exception {
    File primaryXmlFile = new File(repodataDir, PRIMARY_XML_GZ);
    log.info("Testing file {} ...", primaryXmlFile);

    GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(primaryXmlFile));
    Diff xmlDiff = new Diff(createTemplateFileReader(templateName, PRIMARY_XML),
      new InputStreamReader(gzipInputStream));
    xmlDiff.overrideDifferenceListener(new TimeStampIgnoringDifferenceListener());
    try {
      assertTrue(xmlDiff.toString(), xmlDiff.similar());
    } catch (AssertionError e) {
      log.error("Primary.xml failed test for template {} with following content : {}", templateName,
        IOUtils.toString(new GZIPInputStream(new FileInputStream(primaryXmlFile))));
      throw e;
    }
  }

  private static void assertRepomdXml(File repodataDir, String templateName) throws Exception {
    Diff xmlDiff = new Diff(createTemplateFileReader(templateName, REPOMD_XML),
      new FileReader(new File(repodataDir, REPOMD_XML)));

    xmlDiff.overrideDifferenceListener(new TimeStampIgnoringDifferenceListener());
    assertTrue(xmlDiff.toString(), xmlDiff.similar());
  }

  public static FileReader createTemplateFileReader(String templateName, String fileName) throws FileNotFoundException {
    return new FileReader(new File(TEMPLATE_DIR, templateName + File.separator + fileName));
  }

  public static File createDummyRpm(String name, String version) throws NoSuchAlgorithmException, IOException {
    return createDummyRpm(name, version, new File(BASE_TMP_FILE, "dummy-rpms"));
  }

  public static File createDummyRpm(String name, String version, File outputDirectory) throws NoSuchAlgorithmException,
    IOException {
    Builder rpmBuilder = new Builder();
    rpmBuilder.setVendor("IS24");
    rpmBuilder.setGroup("is24");
    rpmBuilder.setPackager("maven - " + System.getProperty("user.name"));
    try {
      rpmBuilder.setBuildHost(InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      throw new RuntimeException("Could not determine hostname.", e);
    }
    rpmBuilder.setPackage(name, version, "1");
    rpmBuilder.setPlatform(NOARCH, LINUX);
    rpmBuilder.setType(BINARY);
    rpmBuilder.setSourceRpm("dummy-source-rpm-because-yum-needs-this");

    outputDirectory.mkdirs();

    String filename = rpmBuilder.build(outputDirectory);
    return new File(outputDirectory, filename);
  }

  public static File copyToTempDir(File srcDir) throws IOException {
    final File destDir = new File(BASE_TMP_FILE, RandomStringUtils.randomAlphabetic(20));
    copyDirectory(srcDir, destDir);
    return destDir;
  }
}
