package org.sonatype.nexus.repository.http;

import java.util.Map;

import org.sonatype.nexus.repository.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.repository.http.ConnectionConfig;
import org.sonatype.nexus.repository.http.HttpClientConfig;
import org.sonatype.nexus.repository.http.HttpClientConfigMarshallerImpl;
import org.sonatype.nexus.repository.http.HttpProxyConfig;
import org.sonatype.nexus.repository.http.NtlmAuthenticationConfig;
import org.sonatype.nexus.repository.http.NtlmAuthenticationConfigMarshaller;
import org.sonatype.nexus.repository.http.ProxyConfig;
import org.sonatype.nexus.repository.http.UsernameAuthenticationConfig;
import org.sonatype.nexus.repository.http.UsernameAuthenticationConfigMarshaller;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link HttpClientConfigMarshallerImpl}.
 */
public class HttpClientConfigMarshallerImplTest
    extends TestSupport
{
  private Map<String, AuthenticationConfigMarshaller> authMarshallers = Maps.newHashMap();

  @Before
  public void setUp() {
    authMarshallers.put("username", new UsernameAuthenticationConfigMarshaller());
    authMarshallers.put("ntlm", new NtlmAuthenticationConfigMarshaller());
  }

  /**
   * Verify config marshalling to NestedAttributesMap.
   */
  @Test
  public void toMap() {
    final HttpClientConfig config = new HttpClientConfig()
        .withConnectionConfig(new ConnectionConfig()
                .withTimeout(1)
                .withRetries(2)
                .withUrlParameters("params")
                .withUserAgentCustomisation("ua")
                .withUseTrustStore(true)
        )
        .withAuthenticationConfig(new UsernameAuthenticationConfig()
                .withUsername("foo")
                .withPassword("bar")
        )
        .withProxyConfig(new ProxyConfig()
                .withHttpProxyConfig(new HttpProxyConfig()
                        .withHostname("httpHost")
                        .withPort(3)
                        .withAuthenticationConfig(new UsernameAuthenticationConfig()
                                .withUsername("foo1")
                                .withPassword("bar1")
                        )
                )
                .withHttpsProxyConfig(new HttpProxyConfig()
                        .withHostname("httpsHost")
                        .withPort(4)
                        .withAuthenticationConfig(new NtlmAuthenticationConfig()
                                .withUsername("foo2")
                                .withPassword("bar2")
                                .withNtlmHost("ntlmH")
                                .withNtlmDomain("ntlmD")
                        )
                )
                .withNonProxyHosts(new String[]{"h1", "h2"})
        );
    final NestedAttributesMap map = new NestedAttributesMap("key", Maps.<String, Object>newHashMap());

    new HttpClientConfigMarshallerImpl(authMarshallers).toMap(config, map);

    System.err.println(map.toString());

    final NestedAttributesMap http = map.child("http");
    final NestedAttributesMap connection = http.child("connection");
    assertThat(connection.get("timeout", Integer.class), is(1));
    assertThat(connection.get("retries", Integer.class), is(2));
    assertThat(connection.get("urlParameters", String.class), is("params"));
    assertThat(connection.get("userAgentCustomisation", String.class), is("ua"));
    assertThat(connection.get("useTrustStore", Boolean.class), is(true));

    final NestedAttributesMap auth = http.child("authentication");
    assertThat(auth.get("type", String.class), is("username"));
    assertThat(auth.get("username", String.class), is("foo"));
    assertThat(auth.get("password", String.class), is("bar"));
    assertThat(auth.get("ntlmHost", String.class), is(nullValue()));
    assertThat(auth.get("ntlmDomain", String.class), is(nullValue()));

    final NestedAttributesMap proxy = http.child("proxy");
    final NestedAttributesMap proxyHttp = proxy.child("http");
    assertThat(proxyHttp.get("hostname", String.class), is("httpHost"));
    assertThat(proxyHttp.get("port", Integer.class), is(3));
    final NestedAttributesMap proxyHttpAuth = proxyHttp.child("authentication");
    assertThat(proxyHttpAuth.get("type", String.class), is("username"));
    assertThat(proxyHttpAuth.get("username", String.class), is("foo1"));
    assertThat(proxyHttpAuth.get("password", String.class), is("bar1"));
    assertThat(proxyHttpAuth.get("ntlmHost", String.class), is(nullValue()));
    assertThat(proxyHttpAuth.get("ntlmDomain", String.class), is(nullValue()));

    final NestedAttributesMap proxyHttps = proxy.child("https");
    assertThat(proxyHttps.get("hostname", String.class), is("httpsHost"));
    assertThat(proxyHttps.get("port", Integer.class), is(4));
    final NestedAttributesMap proxyHttpsAuth = proxyHttps.child("authentication");
    assertThat(proxyHttpsAuth.get("type", String.class), is("ntlm"));
    assertThat(proxyHttpsAuth.get("username", String.class), is("foo2"));
    assertThat(proxyHttpsAuth.get("password", String.class), is("bar2"));
    assertThat(proxyHttpsAuth.get("ntlmHost", String.class), is("ntlmH"));
    assertThat(proxyHttpsAuth.get("ntlmDomain", String.class), is("ntlmD"));

    assertThat(proxy.get("nonProxyHosts", String.class), is("h1,h2"));
  }

  /**
   * Verify config un-marshalling from NestedAttributesMap.
   */
  @Test
  public void fromMap() {
    Map<String, Object> innerMap = Maps.newHashMap();
    final NestedAttributesMap map = new NestedAttributesMap("key", innerMap);

    final NestedAttributesMap http = map.child("http");
    final NestedAttributesMap connection = http.child("connection");
    connection.set("timeout", 1);
    connection.set("retries", 2);
    connection.set("urlParameters", "params");
    connection.set("userAgentCustomization", "ua");
    connection.set("useTrustStore", Boolean.TRUE);

    final NestedAttributesMap httpAuth = http.child("authentication");
    httpAuth.set("type", "username");
    httpAuth.set("username", "foo");
    httpAuth.set("password", "bar");

    final NestedAttributesMap proxy = http.child("proxy");
    final NestedAttributesMap proxyHttp = proxy.child("http");
    proxyHttp.set("hostname", "httpHost");
    proxyHttp.set("port", 3);
    proxyHttp.child("authentication").set("type", "username");
    proxyHttp.child("authentication").set("username", "foo1");
    proxyHttp.child("authentication").set("password", "bar1");

    final NestedAttributesMap proxyHttps = proxy.child("https");
    proxyHttps.set("hostname", "httpsHost");
    proxyHttps.set("port", 4);
    final NestedAttributesMap proxyHttpsAuth = proxyHttps.child("authentication");
    proxyHttpsAuth.set("type", "ntlm");
    proxyHttpsAuth.set("username", "foo2");
    proxyHttpsAuth.set("password", "bar2");
    proxyHttpsAuth.set("ntlmHost", "ntlmH");
    proxyHttpsAuth.set("ntlmDomain", "ntlmD");

    proxy.set("nonProxyHosts", "h1,h2");

    System.err.println(map);

    HttpClientConfig config = new HttpClientConfigMarshallerImpl(authMarshallers).fromMap(map);

    assertThat(config.getConnectionConfig().getTimeout(), is(1));
    assertThat(config.getConnectionConfig().getRetries(), is(2));
    assertThat(config.getConnectionConfig().getUrlParameters(), is("params"));
    assertThat(config.getConnectionConfig().getUserAgentCustomisation(), is("ua"));
    assertThat(config.getConnectionConfig().getUseTrustStore(), is(true));

    UsernameAuthenticationConfig a1 = (UsernameAuthenticationConfig) config.getAuthenticationConfig();
    assertThat(a1.getUsername(), is("foo"));
    assertThat(a1.getPassword(), is("bar"));

    assertThat(config.getProxyConfig().getHttpProxyConfig().getHostname(), is("httpHost"));
    assertThat(config.getProxyConfig().getHttpProxyConfig().getPort(), is(3));
    UsernameAuthenticationConfig a2 = (UsernameAuthenticationConfig) config.getProxyConfig().getHttpProxyConfig()
        .getAuthenticationConfig();
    assertThat(a2.getUsername(), is("foo1"));
    assertThat(a2.getPassword(), is("bar1"));

    assertThat(config.getProxyConfig().getHttpsProxyConfig().getHostname(), is("httpsHost"));
    assertThat(config.getProxyConfig().getHttpsProxyConfig().getPort(), is(4));
    NtlmAuthenticationConfig a3 = (NtlmAuthenticationConfig) config.getProxyConfig().getHttpsProxyConfig()
        .getAuthenticationConfig();
    assertThat(a3.getUsername(), is("foo2"));
    assertThat(a3.getPassword(), is("bar2"));
    assertThat(a3.getNtlmHost(), is("ntlmH"));
    assertThat(a3.getNtlmDomain(), is("ntlmD"));

    assertThat(config.getProxyConfig().getNonProxyHosts(), is(new String[]{"h1", "h2"}));
  }

}