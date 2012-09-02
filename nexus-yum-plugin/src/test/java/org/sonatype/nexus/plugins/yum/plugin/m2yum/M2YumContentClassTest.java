package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

import com.sonatype.nexus.staging.api.AbstractStagingProfilePlexusResource;

public class M2YumContentClassTest {

  @Test
  public void shouldBeCompatibleToM2() throws Exception {
    ContentClass m2yum = new M2YumContentClass();
    assertThat(m2yum.isCompatible(new M2YumContentClass()), is(true));
    assertThat(m2yum.isCompatible(new Maven2ContentClass()), is(true));
  }

  @Test
  public void shouldDetectStagingRepoValidation() throws Exception {
    final AbstractStagingProfilePlexusResource resource = new AbstractStagingProfilePlexusResource();
    assertThat(resource.validateProfile(), is("maven2"));
  }
}
