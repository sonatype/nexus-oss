package org.sonatype.nexus.testsuite.nuget;

import java.net.URL;

import javax.inject.Inject;

import com.sonatype.nexus.repository.nuget.internal.NugetHostedRecipe;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.WritePolicy;

import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests NuGet hosted repositories.
 *
 * @since 3.0
 */
@ExamReactorStrategy(PerClass.class)
public class NugetHostedPushIT
    extends NugetITSupport
{
  @Inject
  private RepositoryManager repositoryManager;

  @Test
  public void launchHostedRepository() throws Exception {

    final Configuration config = new Configuration();
    config.setRepositoryName("nuget-test-hosted");
    config.setRecipeName(NugetHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());

    final Repository repository = createOrReplaceRepository(config);

    final URL url = resolveUrl(nexusUrl, "/repository/nuget-test-hosted/.metadata");
    waitFor(responseFrom(url));
  }

  /**
   * Creates a repository, first removing an existing one if necessary.
   */
  private Repository createOrReplaceRepository(final Configuration config) throws Exception {
    waitFor(responseFrom(nexusUrl));
    repositoryManager.get(config.getRepositoryName());

    final Repository repository = repositoryManager.create(config);
    //log.debug("Created repository {}", config.getRepositoryName());
    return repository;
  }
}
