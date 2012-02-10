package de.is24.nexus.yum.plugin.m2yum;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;

import de.is24.nexus.yum.AbstractRepositoryTester;

public class M2YumGroupRepositoryTest extends AbstractRepositoryTester {

  @Inject
  private RepositoryTypeRegistry typeRegistry;

  @Test
  public void shouldRetrieveNewYumGroupRepositoryType() throws Exception {
    RepositoryTypeDescriptor desc = typeRegistry.getRepositoryTypeDescriptor(GroupRepository.class, "maven2yum");
    assertThat(desc, notNullValue());
  }

  @Test
  public void shouldHasFacet() throws Exception {
    GroupRepository repo = getContainer().lookup(GroupRepository.class, "maven2yum");
    assertThat(repo.getRepositoryKind().isFacetAvailable(M2YumGroupRepository.class), Matchers.is(true));
  }

}
