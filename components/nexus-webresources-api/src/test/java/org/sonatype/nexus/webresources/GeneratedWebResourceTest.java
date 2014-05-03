package org.sonatype.nexus.webresources;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link GeneratedWebResource}.
 */
public class GeneratedWebResourceTest
  extends TestSupport
{
  private GeneratedWebResource underTest;

  @Before
  public void setUp() throws Exception {
    this.underTest = new GeneratedWebResource() {
      @Override
      protected byte[] generate() throws IOException {
        return "test".getBytes();
      }

      @Override
      public String getPath() {
        return "/test";
      }

      @Nullable
      @Override
      public String getContentType() {
        return PLAIN;
      }
    };
  }

  @Test
  public void nonPrepared_basicDetails() throws Exception {
    assertThat(underTest.getPath(), is("/test"));
    assertThat(underTest.isCacheable(), is(false));
    assertThat(underTest.getContentType(), is(WebResource.PLAIN));
    assertThat(underTest.getLastModified(), not(WebResource.UNKNOWN_LAST_MODIFIED));
  }

  @Test
  public void getSize_requiresPreperation() throws Exception {
    try {
      underTest.getSize();
      fail();
    }
    catch (UnsupportedOperationException expected) {
      // ignore
    }

    WebResource prepared = underTest.prepare();
    assertThat(prepared, notNullValue());
    assertThat(prepared.getSize(), is((long)"test".getBytes().length));
  }

  @Test
  public void getInputStream_requiresPreperation() throws Exception {
    try {
      underTest.getInputStream();
      fail();
    }
    catch (UnsupportedOperationException expected) {
      // ignore
    }

    WebResource prepared = underTest.prepare();
    assertThat(prepared, notNullValue());
    assertThat(prepared.getInputStream(), notNullValue());
  }
}
