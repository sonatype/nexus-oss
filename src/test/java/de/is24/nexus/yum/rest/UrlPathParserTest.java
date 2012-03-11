package de.is24.nexus.yum.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;

import de.is24.nexus.yum.rest.domain.UrlPathInterpretation;


public class UrlPathParserTest {
  private static final String DOMAIN = "http://localhost:8081";
  private static final String BASE_PATH = "/nexus/service/local";
  private static final String REPO_PATH = BASE_PATH + "/yum/snapshots/76.0.1";
  private static final String BASE_URL = DOMAIN + BASE_PATH;
  private static final String REPO_URL = DOMAIN + REPO_PATH;
  private static final String REPOMD = "repodata/repomd.xml";

  @Test(expected = ResourceException.class)
  public void shouldThrowExceptionIfPrefixIsNotInUri() throws Exception {
    createInterpretation("yum", 2, BASE_URL + "/bla/bla/blup/repod.xml");
  }

  @Test
  public void shouldFindPrefix() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum", 2, REPO_URL + "/" + REPOMD);
    assertEquals(REPO_URL, interpretation.getRepositoryUrl().toString());
    assertEquals(REPOMD, interpretation.getPath());
  }

  @Test(expected = ResourceException.class)
  public void shouldThrowExceptionForWrongUri() throws Exception {
    new UrlPathParser("dfle//://./7", "yum", 2).interprete(new Request(Method.GET, REPO_URL + "/" + REPOMD));
  }

  @Test
  public void shouldFindIndex() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum", 2, REPO_URL);
    assertTrue(interpretation.isIndex());
    assertTrue(interpretation.isRedirect());
    assertEquals(REPO_PATH + "/", interpretation.getRedirectUri());
  }

  @Test
  public void shouldFindIndex2() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum", 2, REPO_URL + "/");
    assertTrue(interpretation.isIndex());
  }

  @Test
  public void shouldFindIndex3() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum", 2, REPO_URL + "/repodata");
    assertTrue(interpretation.isIndex());
    assertTrue(interpretation.isRedirect());
    assertEquals(REPO_PATH + "/repodata/", interpretation.getRedirectUri());
  }

  @Test
  public void shouldFindIndex4() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum", 2, REPO_URL + "/repodata/");
    assertTrue(interpretation.isIndex());
  }

  @Test
  public void shouldRetrieveRpmFileInBaseDir() throws Exception {
    UrlPathInterpretation interpretation = createInterpretation("yum-repos", 0,
      BASE_URL +
      "/yum-repos/is24-rel-try-next-1.0-1-1.noarch.rpm");
    assertFalse(interpretation.isIndex());
    assertFalse(interpretation.isRedirect());
    assertEquals(interpretation.getPath(), "is24-rel-try-next-1.0-1-1.noarch.rpm");
  }

  private UrlPathInterpretation createInterpretation(String prefix, int segmentsAfterPrefix, String uri)
    throws ResourceException {
    return new UrlPathParser(BASE_URL, prefix, segmentsAfterPrefix).interprete(new Request(Method.GET, uri));
  }

}
