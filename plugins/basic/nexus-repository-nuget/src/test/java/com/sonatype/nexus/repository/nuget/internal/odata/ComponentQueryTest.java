package com.sonatype.nexus.repository.nuget.internal.odata;

import java.util.Map;

import com.sonatype.nexus.repository.nuget.internal.odata.ComponentQuery.Builder;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ComponentQueryTest
{
  @Test
  public void testHasWhere() {
    final Builder b = new Builder();

    assertThat(b.hasWhere(), is(equalTo(false)));

    b.where("   "); // this will get trimmed
    assertThat(b.hasWhere(), is(equalTo(false)));

    b.where("placebo");
    assertThat(b.hasWhere(), is(equalTo(true)));
  }

  @Test
  public void testAnonymousParameter() {
    final Builder b = new Builder();

    b.where("x = ").param("placebo");

    final ComponentQuery query = b.build();

    assertThat(query.getWhere(), is(equalTo("x = :p0")));
    final Map<String, Object> parameters = query.getParameters();

    assertThat(parameters.size(), is(equalTo(1)));
    assertThat((String) parameters.get("p0"), is(equalTo("placebo")));
  }
}