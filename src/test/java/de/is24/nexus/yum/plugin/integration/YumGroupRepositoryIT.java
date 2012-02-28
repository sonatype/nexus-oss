package de.is24.nexus.yum.plugin.integration;

import static de.is24.nexus.yum.plugin.m2yum.M2YumGroupRepository.ID;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.sonatype.nexus.proxy.repository.Repository;

public class YumGroupRepositoryIT extends AbstractNexusTestBase {

  private static final String MEMBER_REPO1 = "member-repo-1";
  private static final String MEMBER_REPO2 = "member-repo-2";
  private static final String GROUP_REPO_ID = "group-repo-test";
  private static final String DUMMY_ARTIFACT1 = "groupRepoArtiFoo";
  private static final String DUMMY_ARTIFACT2 = "groupRepoArtiBla";
  private static final String GROUP_ID = "groupRepo";
  private static final String ARTIFACT_VERSION1 = "1";
  private static final String ARTIFACT_VERSION2 = "2";

  @Test
  public void shouldRegenerateGroupRepoWhenRpmGetsUploaded() throws Exception {
    givenGroupRepoWith2Rpms();
    String primaryXml = getGroupRepoPrimaryXml();
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT1));
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT2));
  }

  @Test
  public void shouldRegenerateGroupRepoWhenMemberRepoIsRemoved() throws Exception {
    givenGroupRepoWith2Rpms();
    removeMemberRepo(MEMBER_REPO2);
    String primaryXml = getGroupRepoPrimaryXml();
    assertThat(primaryXml, containsString(DUMMY_ARTIFACT1));
    assertThat(primaryXml, not(containsString(DUMMY_ARTIFACT2)));
  }


  @Test
  public void shouldRegenerateGroupRepoWhenMemberRepoIsAdded() throws Exception {
    // TODO
  }

  private void removeMemberRepo(String memberRepo2) throws Exception {
    executeGet(format("repo_groups/%s", memberRepo2));
    wait(5, SECONDS);
  }

  private String getGroupRepoPrimaryXml() throws IOException, AuthenticationException {
    return gzipResponseContent(executeGetWithResponse(NEXUS_BASE_URL + "/content/groups/" + GROUP_REPO_ID + "/repodata/primary.xml.gz"));
  }

  private void givenGroupRepoWith2Rpms() throws Exception, AuthenticationException, UnsupportedEncodingException, IOException,
      ClientProtocolException, InterruptedException, NoSuchAlgorithmException {
    givenTestRepository(MEMBER_REPO1);
    givenTestRepository(MEMBER_REPO2);
    consume(givenGroupRepository(GROUP_REPO_ID, ID, repo(MEMBER_REPO1), repo(MEMBER_REPO2)).getEntity());
    wait(5, SECONDS);
    assertEquals(deployRpm(DUMMY_ARTIFACT1, GROUP_ID, ARTIFACT_VERSION1, MEMBER_REPO1), SC_CREATED);
    assertEquals(deployRpm(DUMMY_ARTIFACT2, GROUP_ID, ARTIFACT_VERSION2, MEMBER_REPO2), SC_CREATED);
    wait(5, SECONDS);
  }

  private Repository repo(String repoId) {
    Repository repo = mock(Repository.class);
    when(repo.getId()).thenReturn(repoId);
    when(repo.getName()).thenReturn(repoId);
    return repo;
  }
}
