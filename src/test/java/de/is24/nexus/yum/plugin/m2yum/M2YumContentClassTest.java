package de.is24.nexus.yum.plugin.m2yum;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

public class M2YumContentClassTest {

  @Test
  public void shouldBeCompatibleToM2() throws Exception {
    ContentClass m2yum = new M2YumContentClass();
    assertThat(m2yum.isCompatible(new M2YumContentClass()), is(true));
    assertThat(m2yum.isCompatible(new Maven2ContentClass()), is(true));
  }
}
