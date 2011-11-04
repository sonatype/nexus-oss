package de.is24.nexus.yum.service.impl;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.PRIMARY_XML;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.createTemplateFileReader;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import org.custommonkey.xmlunit.Diff;
import org.junit.Ignore;
import org.junit.Test;
import de.is24.nexus.yum.repository.xml.TimeStampIgnoringDifferenceListener;


@Ignore
public class BuildAgentPrimaryXmlPlaygroundTest {
  public static final String BUILD_AGENT_STR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<metadata xmlns=\"http://linux.duke.edu/metadata/common\" xmlns:rpm=\"http://linux.duke.edu/metadata/rpm\" packages=\"1\">\n" +
    "<package type=\"rpm\"><name>is24-rel-snapshots-0.1.2-snapshot-repo</name><arch>noarch</arch><version epoch=\"0\" ver=\"1\" rel=\"1\"/><checksum type=\"sha\" pkgid=\"YES\">558f424461d4c75506b1550c1a99c37fecc988eb</checksum><summary>(none)</summary><description>(none)</description><packager>maven - webadmin</packager><url/><time file=\"1310046854\" build=\"1310046854\"/><size package=\"1877\" installed=\"252\" archive=\"672\"/><location xml:base=\"http://localhost:8081/nexus/service/local/yum-repos\" href=\"is24-rel-snapshots-0.1.2-snapshot-repo-1-1.noarch.rpm\"/><format><rpm:license/><rpm:vendor>IS24</rpm:vendor><rpm:group>is24</rpm:group><rpm:buildhost>devbui07.be.test.is24.loc</rpm:buildhost><rpm:sourcerpm>dummy-source-rpm-because-yum-needs-this</rpm:sourcerpm><rpm:header-range start=\"280\" end=\"1573\"/><rpm:provides><rpm:entry name=\"is24-rel-snapshots-0.1.2-snapshot-repo\" flags=\"EQ\" epoch=\"0\" ver=\"1\" rel=\"1\"/></rpm:provides><rpm:requires><rpm:entry name=\"rpmlib(PayloadFilesHavePrefix)\" flags=\"LE\" epoch=\"0\" ver=\"4.0\" rel=\"1\"/><rpm:entry name=\"rpmlib(CompressedFileNames)\" flags=\"LE\" epoch=\"0\" ver=\"3.0.4\" rel=\"1\"/><rpm:entry name=\"rpmlib(VersionedDependencies)\" flags=\"LE\" epoch=\"0\" ver=\"3.0.3\" rel=\"1\"/></rpm:requires><file>/etc/yum.repos.d/is24-rel-snapshots-0.1.2-snapshot.repo</file><file type=\"dir\">/etc/yum.repos.d</file></format></package>\n" +
    "</metadata>";

  @Test
  public void shouldTestBuildAgentConfig() throws Exception {
    Diff xmlDiff = new Diff(createTemplateFileReader("yum-repos", PRIMARY_XML), new StringReader(BUILD_AGENT_STR));
    xmlDiff.overrideDifferenceListener(new TimeStampIgnoringDifferenceListener());
    assertTrue(xmlDiff.toString(), xmlDiff.similar());
  }
}
