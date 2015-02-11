package com.sonatype.nexus.repository.nuget.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.fileupload.util.Streams;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TempStreamSupplierTest
{
  final String content = "foo";

  @Test
  public void testGetTwice() throws Exception {
    TempStreamSupplier underTest = new TempStreamSupplier(content());

    try (InputStream i1 = underTest.get(); InputStream i2 = underTest.get()) {
      assertThat(Streams.asString(i1), is(content));
      assertThat(Streams.asString(i2), is(content));
    }
  }

  private InputStream content() {
    return new ByteArrayInputStream(content.getBytes());
  }
}