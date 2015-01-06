package org.sonatype.nexus.web.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CookieFilterTest
    extends TestSupport
{

  private static String COOKIE_1_INSECURE = "JSESSIONID=98a766bc-bc33-4b3c-9d9f-d3bb85b0cf00; Path=/nexus; HttpOnly";

  private static String COOKIE_2_INSECURE = "simple=cookie";

  private static String COOKIE_3_SECURE = "NXSESSIONID=98a766bc-bc33-4b3c-9d9f-d3bb85b0cf00; Path=/nexus; HttpOnly; Secure";

  private static String COOKIE_4_SECURE = "rememberMe=deleteMe; Path=/nexus; Secure; HttpOnly;";

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpServletRequest request;

  @Mock
  private FilterChain filterChain;

  @Mock
  private ServletResponse notInstanceOfHttpServletResponse;

  @Test
  public void ifRequestIsSecureSetSecureCookieAttributeWhenMissing() throws Exception {
    when(request.isSecure()).thenReturn(true);

    List<String> data = Arrays.asList(COOKIE_1_INSECURE, COOKIE_2_INSECURE, COOKIE_3_SECURE, COOKIE_4_SECURE);
    final Collection<String> cookies = Collections.unmodifiableCollection(data);
    when(response.getHeaders("Set-Cookie")).thenReturn(cookies);

    CookieFilter cookieFilter = new CookieFilter();
    cookieFilter.doFilter(request, response, filterChain);

    verify(response).setHeader("Set-Cookie", COOKIE_1_INSECURE + "; Secure");
    verify(response).addHeader("Set-Cookie", COOKIE_2_INSECURE + "; Secure");
    verify(response).addHeader("Set-Cookie", COOKIE_3_SECURE);
    verify(response).addHeader("Set-Cookie", COOKIE_4_SECURE);
  }

  @Test
  public void ifRequestIsNotSecureDoNotChangeCookieAttributes() throws Exception {
    when(request.isSecure()).thenReturn(false);

    List<String> data = Arrays.asList(COOKIE_1_INSECURE, COOKIE_3_SECURE);
    final Collection<String> cookies = Collections.unmodifiableCollection(data);
    when(response.getHeaders("Set-Cookie")).thenReturn(cookies);

    CookieFilter cookieFilter = new CookieFilter();
    cookieFilter.doFilter(request, response, filterChain);

    verifyZeroInteractions(response);
  }

  @Test
  public void ifNotInstanceOfHttpServletResponseDoNotProcessResponse() throws Exception {
    CookieFilter cookieFilter = new CookieFilter();
    cookieFilter.doFilter(request, notInstanceOfHttpServletResponse, filterChain);

    verifyZeroInteractions(response);
  }

  @Test
  public void doNotChangeTheResponseIfNoCookieHeaders() throws Exception {
    when(request.isSecure()).thenReturn(true);

    CookieFilter cookieFilter = new CookieFilter();
    cookieFilter.doFilter(request, response, filterChain);

    verify(response, times(0)).setHeader(anyString(), anyString());
    verify(response, times(0)).addHeader(anyString(), anyString());
  }

}