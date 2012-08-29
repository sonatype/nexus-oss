package org.sonatype.nexus.plugins.yum.plugin.integration;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumGroupRepository.ID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Rule;
import org.junit.Test;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.test.os.IgnoreOn;
import org.sonatype.nexus.test.os.OsTestRule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class YumGroupRepositoryIT extends AbstractNexusTestBase {

  private static final BasicHeader CONTENT_XML = new BasicHeader("Content-Type", "application/xml");
  private static final String MEMBER_REPO1 = "member-repo-1";
  private static final String MEMBER_REPO2 = "member-repo-2";
  private static final String MEMBER_REPO3 = "member-repo-3";
  private static final String GROUP_REPO_ID = "group-repo-test";
  private static final String GROUP_REPO_URL = format("%s/repo_groups/%s", SERVICE_BASE_URL, GROUP_REPO_ID);
  private static final String DUMMY_ARTIFACT1 = "groupRepoArtiFoo";
  private static final String DUMMY_ARTIFACT2 = "groupRepoArtiBla";
  private static final String DUMMY_ARTIFACT3 = "groupRepoArtiBoo";
  private static final String GROUP_ID = "groupRepo";
  private static final String ARTIFACT_VERSION1 = "1";
  private static final String ARTIFACT_VERSION2 = "2";
  private static final String ARTIFACT_VERSION3 = "3";

  @Rule
  public OsTestRule osTestRule = new OsTestRule();

  @Test
  @IgnoreOn("mac")
  public void shouldRegenerateGroupRepoWhenRpmGetsUploaded() throws Exception {
    givenGroupRepoWith2Rpms();
    String primaryXml = getGroupRepoPrimaryXml();
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT1));
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT2));
  }

  @Test
  @IgnoreOn("mac")
  public void shouldRegenerateGroupRepoWhenMemberRepoIsRemoved() throws Exception {
    givenGroupRepoWith2Rpms();
    removeMemberRepo(MEMBER_REPO2);
    wait(5, SECONDS);
    String primaryXml = getGroupRepoPrimaryXml();
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT1));
    assertThat(primaryXml, not(containsString(DUMMY_ARTIFACT2)));
  }

  @Test
  @IgnoreOn("mac")
  public void shouldRegenerateGroupRepoWhenMemberRepoIsAdded() throws Exception {
    givenGroupRepoWith2Rpms();
    givenTestRepository(MEMBER_REPO3);
    wait(5, SECONDS);
    deployRpmToRepo(DUMMY_ARTIFACT3, GROUP_ID, ARTIFACT_VERSION3, MEMBER_REPO3);
    wait(5, SECONDS);
    addMemberRepo(MEMBER_REPO3);
    wait(5, SECONDS);
    String primaryXml = getGroupRepoPrimaryXml();
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT1));
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT2));
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT3));
  }

  private void addMemberRepo(String memberRepo) throws Exception {
    String repoXml = executeGet(GROUP_REPO_URL);
    repoXml = repoXml.replace("</repositories>",
        format("<repo-group-member><id>%s</id><name>%s</name><resourceURI>%s</resourceURI></repo-group-member></repositories>", memberRepo,
            memberRepo, GROUP_REPO_URL + "/" + memberRepo));
    consume(executePut(GROUP_REPO_URL, new StringEntity(repoXml), CONTENT_XML).getEntity());
  }

  private void removeMemberRepo(String memberRepo) throws Exception {
    final String repoJson = removeMemberRepoFromXml(memberRepo);
    consume(executePut(GROUP_REPO_URL, new StringEntity(repoJson), CONTENT_XML).getEntity());
  }

  private String removeMemberRepoFromXml(String memberRepo) throws Exception {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    final Document document = dbf.newDocumentBuilder().parse(GROUP_REPO_URL);

    final XPath xpath = XPathFactory.newInstance().newXPath();
    final XPathExpression expression = xpath.compile("//repo-group-member[id/text()='" + memberRepo + "']");

    final Node memberRepoNode = (Node) expression.evaluate(document, XPathConstants.NODE);
    memberRepoNode.getParentNode().removeChild(memberRepoNode);

    final TransformerFactory tf = TransformerFactory.newInstance();
    final Transformer t = tf.newTransformer();
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      t.transform(new DOMSource(document), new StreamResult(outputStream));
      return outputStream.toString();
    } finally {
      outputStream.close();
    }
  }

  private String getGroupRepoPrimaryXml() throws IOException, AuthenticationException {
    return gzipResponseContent(executeGetWithResponse(NEXUS_BASE_URL + "/content/groups/" + GROUP_REPO_ID + "/repodata/primary.xml.gz"));
  }

  private void givenGroupRepoWith2Rpms() throws Exception, AuthenticationException, UnsupportedEncodingException, IOException,
      ClientProtocolException, InterruptedException, NoSuchAlgorithmException {
    givenTestRepository(MEMBER_REPO1);
    givenTestRepository(MEMBER_REPO2);
    givenGroupRepository(GROUP_REPO_ID, ID, repo(MEMBER_REPO1), repo(MEMBER_REPO2));
    wait(5, SECONDS);
    deployRpmToRepo(DUMMY_ARTIFACT1, GROUP_ID, ARTIFACT_VERSION1, MEMBER_REPO1);
    deployRpmToRepo(DUMMY_ARTIFACT2, GROUP_ID, ARTIFACT_VERSION2, MEMBER_REPO2);
    wait(5, SECONDS);
  }

  private Repository repo(String repoId) {
    Repository repo = mock(Repository.class);
    when(repo.getId()).thenReturn(repoId);
    when(repo.getName()).thenReturn(repoId);
    return repo;
  }
}
