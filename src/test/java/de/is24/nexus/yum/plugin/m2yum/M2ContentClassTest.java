package de.is24.nexus.yum.plugin.m2yum;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;

import de.is24.nexus.yum.AbstractRepositoryTester;

public class M2ContentClassTest extends AbstractRepositoryTester {

  @Test
  public void shouldOverrideDefaultMaven2ContentClass() throws Exception {
    Repository repo = getContainer().lookup(Repository.class, M2Repository.ID);
    assertThat((M2ContentClass) repo.getRepositoryContentClass(), isA(M2ContentClass.class));
  }

}
