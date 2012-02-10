package de.is24.nexus.yum.plugin.m2yum;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.maven.AbstractMavenRepositoryTemplate;

public class M2YumGroupRepositoryTemplate extends AbstractMavenRepositoryTemplate {
  public M2YumGroupRepositoryTemplate(DefaultRepositoryTemplateProvider provider, String id, String description) {
    super(provider, id, description, new M2YumContentClass(), MavenGroupRepository.class, null);
  }

  public M2GroupRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (M2GroupRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration(forWrite);
  }

  @Override
  protected CRepositoryCoreConfiguration initCoreConfiguration() {
    CRepository repo = new DefaultCRepository();

    repo.setId("");
    repo.setName("");

    repo.setProviderRole(GroupRepository.class.getName());
    repo.setProviderHint(M2YumGroupRepository.ID);

    // groups should not participate in searches
    repo.setSearchable(false);

    Xpp3Dom ex = new Xpp3Dom(DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME);
    repo.setExternalConfiguration(ex);

    M2GroupRepositoryConfiguration exConf = new M2GroupRepositoryConfiguration(ex);
    repo.externalConfigurationImple = exConf;

    repo.setWritePolicy(RepositoryWritePolicy.READ_ONLY.name());

    CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration(getTemplateProvider().getApplicationConfiguration(), repo,
        new CRepositoryExternalConfigurationHolderFactory<M2GroupRepositoryConfiguration>() {
          @Override
          public M2GroupRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
            return new M2GroupRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
          }
        });

    return result;
  }
}
