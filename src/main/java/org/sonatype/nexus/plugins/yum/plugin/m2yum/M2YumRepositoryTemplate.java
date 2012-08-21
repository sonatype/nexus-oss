package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.maven.AbstractMavenRepositoryTemplate;


public class M2YumRepositoryTemplate extends AbstractMavenRepositoryTemplate {
  public M2YumRepositoryTemplate(DefaultRepositoryTemplateProvider provider, String id, String description,
    RepositoryPolicy repositoryPolicy) {
    super(provider, id, description, new Maven2ContentClass(), MavenHostedRepository.class, repositoryPolicy);
  }

  @Override
  protected CRepositoryCoreConfiguration initCoreConfiguration() {
    CRepository repo = new DefaultCRepository();

    repo.setId("");
    repo.setName("");

    repo.setProviderRole(Repository.class.getName());
    repo.setProviderHint(M2YumRepository.ID);

    Xpp3Dom ex = new Xpp3Dom(DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME);
    repo.setExternalConfiguration(ex);

    M2RepositoryConfiguration exConf = new M2RepositoryConfiguration(ex);

    // huh? see initConfig classes
    if (getRepositoryPolicy() != null) {
      exConf.setRepositoryPolicy(getRepositoryPolicy());
    }

    repo.externalConfigurationImple = exConf;

    repo.setWritePolicy(RepositoryWritePolicy.ALLOW_WRITE_ONCE.name());
    repo.setNotFoundCacheTTL(1440);
    repo.setIndexable(true);
    repo.setSearchable(true);

    CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration(getTemplateProvider()
      .getApplicationConfiguration(), repo,
      new CRepositoryExternalConfigurationHolderFactory<M2RepositoryConfiguration>() {
        public M2RepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
          return new M2RepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
        }
      });

    return result;
  }


}
