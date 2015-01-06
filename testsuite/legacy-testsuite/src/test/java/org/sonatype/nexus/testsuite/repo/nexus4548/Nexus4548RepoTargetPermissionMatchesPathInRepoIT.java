/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.repo.nexus4548;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.AbstractSecurityTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.security.rest.model.UserResource;

import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.respondsWithStatusCode;

/**
 * IT testing the pattern matching of repository targets for security permissions.
 * A pattern should fully match paths inside the repo, e.g.
 * {@code ^/g/a/v/.*} should match the path inside the repository and not need to take
 * the URL path (/repositories/id/g/a/v/) into account.
 * <p/>
 * The nexus config is set up to allow the user test/test access to ^/g/a/.* via a repo target permission.
 */
public class Nexus4548RepoTargetPermissionMatchesPathInRepoIT
    extends AbstractSecurityTest
{

  public Nexus4548RepoTargetPermissionMatchesPathInRepoIT() {
    super("releases");
  }

  @Override
  protected void prepareSecurity() throws Exception {
    super.prepareSecurity();
    List<String> rolePrivileges = Lists.newArrayList();
    rolePrivileges.add("artifact-create"); //artifact upload
    rolePrivileges.addAll(privilegeIds(createPrivileges("GAV", "119035e2c095")));
    createRole("GAV", rolePrivileges);

    UserResource test = userUtil.getUser(TEST_USER_NAME);
    test.setRoles(asList("GAV"));
    userUtil.updateUser(test);
  }

  @Before
  public void setSecureTest()
      throws IOException
  {
    TestContext ctx = TestContainer.getInstance().getTestContext();
    ctx.setSecureTest(true);
    ctx.setUsername(TEST_USER_NAME);
    ctx.setPassword(TEST_USER_PASSWORD);
  }

  @Override
  protected void runOnce()
      throws Exception
  {
    // disable anonymous access
    GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
    settings.setSecurityAnonymousAccessEnabled(false);
    SettingsMessageUtil.save(settings);
  }

  private void get(final String gavPath, final int code)
      throws IOException
  {
    RequestFacade.doGet(AbstractNexusIntegrationTest.REPOSITORY_RELATIVE_URL + "releases/" + gavPath,
        respondsWithStatusCode(code));
  }

  private HttpResponse put(final String gavPath, final int code)
      throws Exception
  {
    HttpPut putMethod = new HttpPut(getNexusTestRepoUrl() + gavPath);
    putMethod.setEntity(new FileEntity(getTestFile("pom-a.pom"), "text/xml"));

    final HttpResponse response = RequestFacade.executeHTTPClientMethod(putMethod);
    assertThat(response.getStatusLine().getStatusCode(), Matchers.is(code));
    return response;
  }

  private HttpResponse putRest(final String artifactId, final int code)
      throws IOException
  {
    File testFile = getTestFile(String.format("pom-%s.pom", artifactId));
    final HttpResponse response = getDeployUtils().deployPomWithRest("releases", testFile);
    assertThat(response.getStatusLine().getStatusCode(), Matchers.is(code));
    return response;
  }

  @Test
  public void testAccessGranted()
      throws Exception
  {
    get("g/a/v/a-v.pom", 200);
    put("g/a/v/a-v-deploy.pom", 201);
    putRest("a", 201);
  }

  @Test
  public void testAccessDenied()
      throws Exception
  {
    get("g/b/v/b-v.pom", 403);

    put("g/b/v/b-v-deploy.pom", 403);

    final HttpResponse response = putRest("b", 403);
    final String responseBody = EntityUtils.toString(response.getEntity());
    assertThat(responseBody, containsString("<error>"));
  }
}
