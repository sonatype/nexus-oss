package org.sonatype.nexus.template;

import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

public interface RepositoryTemplateStore
{

    String TEMPLATE_REPOSITORY_PREFIX = "repository-";

    /** Repo type hosted. */
    String REPO_TYPE_HOSTED = "hosted";

    /** Repo type proxied. */
    String REPO_TYPE_PROXIED = "proxy";

    /** Repo type virtual (shadow in nexus). */
    String REPO_TYPE_VIRTUAL = "virtual";

    /** Repo type group. */
    String REPO_TYPE_GROUP = "group";

    // ----------------------------------------------------------------------------
    // Repo templates, CRUD
    // ----------------------------------------------------------------------------

    RepositoryBaseResource retrieveTemplate( String id );

    String getRestRepositoryType( Repository repository );

    RepositoryShadowResource getShadowRepositoryTemplate( ShadowRepository shadow );

    RepositoryProxyResource getProxyRepositoryTemplate( ProxyRepository repository );

    RepositoryBaseResource getRepositoryTemplate( Repository repository );

}
