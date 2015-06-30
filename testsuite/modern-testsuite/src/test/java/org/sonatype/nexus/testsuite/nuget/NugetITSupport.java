/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.nuget;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.sonatype.nexus.repository.nuget.internal.NugetHostedRecipe;
import com.sonatype.nexus.repository.nuget.odata.FeedSplicer;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;
import com.sonatype.nexus.repository.nuget.security.NugetApiKey;
import com.sonatype.nexus.repository.nuget.security.NugetApiKeyStore;

import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.nexus.security.realm.RealmManager;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Support for Nuget ITs
 */
public abstract class NugetITSupport
    extends RepositoryTestSupport
{
  public static final String VISUAL_STUDIO_INITIAL_COUNT_QUERY =
      "Search()/$count?$filter=IsLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  public static final String VISUAL_STUDIO_INITIAL_FEED_QUERY =
      "Search()?$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=30&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  public static final int VS_DEFAULT_PAGE_REQUEST_SIZE = 30;

  @Inject
  private NugetApiKeyStore keyStore;

  @Inject
  private RealmManager realmManager;

  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        withHttps(),
        wrappedBundle(maven("org.apache.httpcomponents", "httpmime").versionAsInProject())
            .overwriteManifest(OverwriteMode.FULL).instructions("DynamicImport-Package=*"),
        mavenBundle("org.sonatype.http-testing-harness", "server-provider").versionAsInProject()
    );
  }

  @Nonnull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(NugetHostedRecipe.NAME);
    config.setOnline(true);

    NestedAttributesMap storage = config.attributes("storage");
    storage.set("blobStoreName", BlobStoreManager.DEFAULT_BLOBSTORE_NAME);
    storage.set("writePolicy", WritePolicy.ALLOW.toString());

    return config;
  }

  @Nonnull
  protected NugetClient nugetClient(final Repository repository) throws Exception {
    final URL url = repositoryBaseUrl(repository);
    waitFor(responseFrom(url));

    final String apiKey = prepareApiKey(new SimplePrincipalCollection("admin", "NexusAuthenticatingRealm"));

    return new NugetClient(clientBuilder().build(), clientContext(), url.toURI(), apiKey);
  }

  @Nonnull
  private String prepareApiKey(final PrincipalCollection admin) {
    char[] apiKey = keyStore.getApiKey(admin);
    log.info("Existing nuget api key for {} is {}", admin, apiKey);
    if (apiKey == null) {
      log.info("Creating new API key for {}", admin);
      apiKey = keyStore.createApiKey(admin);
    }
    return new String(apiKey);
  }

  @Override
  protected void doUseCredentials(final HttpClientBuilder builder) {
    // Do nothing, don't use basic auth credentials
  }

  @Before
  public void enableNugetRealm() {
    log.info("Realm configuration {}", realmManager.getConfiguration());

    //final String realmName = NugetApiKeyRealm.class.getName();
    //final String nugetRealmName = NugetApiKeyRealm.ID;
    final String nugetRealmName = NugetApiKey.NAME;

    final RealmConfiguration config = realmManager.getConfiguration();

    if (!config.getRealmNames().contains(nugetRealmName)) {

      log.info("Adding Nuget realm.");

      config.getRealmNames().add(nugetRealmName);
      realmManager.setConfiguration(config);
      calmPeriod();
    }
    else {
      log.info("Nuget realm already configured.");
    }
  }

  protected ParsedFeed parse(final String feedXml) throws Exception {
    return new ParsedFeed(feedXml);
  }

  protected List<Map<String, String>> parseFeedXml(final String entryXml) throws Exception {
    return parse(entryXml).getEntries();
  }

  protected Integer parseInlineCount(final String entryXml) throws Exception, XmlPullParserException {
    return parse(entryXml).getInlineCount();
  }

  protected String parseNextPageUrl(final String entryXml) throws Exception, XmlPullParserException {
    return parse(entryXml).getNextPageUrl();
  }

  /**
   * A utility to parse feeds
   */
  protected static class ParsedFeed
  {
    private final FeedSplicer splicer;

    private final EntryList consumer;

    private final String nextPageUrl;

    public ParsedFeed(final String feedXml) throws Exception {
      consumer = new EntryList();
      splicer = new FeedSplicer(consumer);
      try (InputStream is = IOUtils.toInputStream(feedXml, "UTF-8")) {
        nextPageUrl = splicer.consumePage(is);
      }
    }

    public List<Map<String, String>> getEntries() {
      return consumer.getEntries();
    }

    @Nullable
    public Integer getInlineCount() {
      return splicer.getCount();
    }

    @Nullable
    public String getNextPageUrl() {
      return nextPageUrl;
    }
  }

  private static class EntryList
      implements ODataConsumer
  {
    private final List<Map<String, String>> entries = new ArrayList<>();

    @Override
    public void consume(final Map<String, String> data) {
      entries.add(data);
    }

    public List<Map<String, String>> getEntries() {
      return entries;
    }
  }
}
