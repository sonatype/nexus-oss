package de.is24.nexus.yum.plugin.integration;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.http.util.EntityUtils.consume;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class CreateGroupRepositoryIT extends AbstractNexusTestBase {
  private static final String ARTIFACT_ID_2 = "staging-bla";
  private static final String ARTIFACT_ID_1 = "staging-foo";
  private static final String GROUP_REPO_ID = "staging-test-group";
  private static final String TARGET_REPO_ID = "production";

  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    givenGroupRepository("dummy-group-repo", "maven2yum");
  }

  @Test
  public void shouldProgateStagingRepoToYumGroupRepo() throws Exception {
    givenGroupRepository(GROUP_REPO_ID, "maven2yum");
    givenClosedStagingRepoWithRpm(ARTIFACT_ID_1, "4.3.2");
    givenClosedStagingRepoWithRpm(ARTIFACT_ID_2, "2.3.4");
    wait(10, SECONDS);
    final HttpResponse response = executeGetWithResponse(NEXUS_BASE_URL + "/content/groups/staging-test-group/repodata/primary.xml.gz");
    final String repoContent = IOUtils.toString(new GZIPInputStream(new ByteArrayInputStream(toByteArray(response.getEntity()))));
    assertThat(response.getStatusLine().getStatusCode(), is(200));
    assertThat(repoContent, containsString(ARTIFACT_ID_1));
    assertThat(repoContent, containsString(ARTIFACT_ID_2));
  }

  protected void givenClosedStagingRepoWithRpm(String artifactId, String artifactVersion) throws Exception, NoSuchAlgorithmException, IOException {
    final String profileName = "test-profile-" + currentTimeMillis();
    final String profileId = givenStagingProfile(profileName, TARGET_REPO_ID, GROUP_REPO_ID);
    reorderProfiles(profileId);
    givenUploadedRpmToStaging("de.is24.staging.test", artifactId, artifactVersion);
    final String repositoryId = getRepositoryId(profileId);
    closeStagingRepo(profileId, repositoryId);
  }

  private String getRepositoryId(String profileId) throws Exception {
    final String content = executeGet("/staging/profile_repositories/" + profileId);
    return content.replaceAll("(?s)(.*<repositoryId>)(.*)(</repositoryId>.*)", "$2");
  }

  private void closeStagingRepo(String profileId, String repositoryId) throws Exception {
    final StringEntity content = new StringEntity("{data: {stagedRepositoryId: '" + repositoryId + "', targetRepositoryId: '"
        + TARGET_REPO_ID + "', description: 'Staging close.'}}");
    final HttpResponse response = executePost("/staging/profiles/" + profileId + "/finish", content, new BasicHeader("Content-Type",
        "application/json"));
    consume(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
  }

  private void reorderProfiles(String topId) throws Exception {
    topId = "'" + topId + "'";
    final List<String> profileIds = getAllProfileIds();
    if (profileIds.contains(topId)) {
      profileIds.remove(topId);
      profileIds.add(0, topId);
    }
    final StringEntity content = new StringEntity("{data: [" + join(profileIds, ",") + "]}");
    final HttpResponse response = executePost("/staging/profile_order", content, new BasicHeader("Content-Type", "application/json"));
    consume(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
  }

  private List<String> getAllProfileIds() throws Exception {
    final String content = executeGet("/staging/profiles");
    final List<String> list = new ArrayList<String>();
    int pos = 0;
    while ((pos = content.indexOf("<id>", pos)) >= 0) {
      int endPos = content.indexOf("</id>", pos);
      list.add("'" + content.substring(pos + 4, endPos) + "'");
      pos = endPos;
    }
    removeDuplicate(list);
    return list;
  }

  protected String givenStagingProfile(String profileId, String targetRepoId, String targetGroupRepoId) throws Exception {
    final StringEntity content = new StringEntity("{data:{name:'" + profileId
        + "', repositoryTemplateId:'maven2yum_hosted_release', repositoryType:'maven2', repositoryTargetId:'5', order: 0, targetGroups:['"
        + targetGroupRepoId + "'], promotionTargetRepository:'" + targetRepoId
        + "', mode:'BOTH', finishNotifyRoles:[], promotionNotifyRoles:[], dropNotifyRoles: [], closeRuleSets: [], promoteRuleSets: []}}");
    final HttpResponse response = executePost("/staging/profiles", content, new BasicHeader("Content-Type", "application/json"));
    final String responseContent = EntityUtils.toString(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
    final JsonNode node = new ObjectMapper().readTree(responseContent);
    return node.get("data").get("id").asText();
  }

  public static void removeDuplicate(List<String> arlList) {
    Set<String> set = new HashSet<String>();
    List<String> newList = new ArrayList<String>();
    for (String element : arlList) {
      if (set.add(element))
        newList.add(element);
    }
    arlList.clear();
    arlList.addAll(newList);
  }
}
