package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;


@Component(role = TemplateProvider.class, hint = DefaultRepositoryTemplateProvider.PROVIDER_ID)
public class M2YumRepositoryTemplateProvider extends DefaultRepositoryTemplateProvider {
  private static final Logger LOG = LoggerFactory.getLogger(M2YumRepositoryTemplateProvider.class);

  @Override
  public TemplateSet getTemplates() {
    LOG.info("Generate M2YumRepositoryTemplates");

    final TemplateSet templates = new TemplateSet(null);

    templates.add(new M2YumRepositoryTemplate(this, "maven2yum_hosted_release", "Maven2 Yum (hosted, release)",
        RepositoryPolicy.RELEASE));
    templates.add(new M2YumRepositoryTemplate(this, "maven2yum_hosted_snapshot", "Maven2 Yum (hosted, snapshot)",
        RepositoryPolicy.SNAPSHOT));
    templates.add(new M2YumGroupRepositoryTemplate(this, "maven2yum_group", "Maven2 Yum (group)"));

    return templates;
  }

}
