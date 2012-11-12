/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

@Component( role = TemplateProvider.class, hint = DefaultRepositoryTemplateProvider.PROVIDER_ID )
public class M2YumRepositoryTemplateProvider
    extends DefaultRepositoryTemplateProvider
{

    @Override
    public TemplateSet getTemplates()
    {
        final TemplateSet templates = new TemplateSet( null );

        templates.add( new M2YumRepositoryTemplate( this, "maven2yum_hosted_release", "Maven2 Yum (hosted, release)",
            RepositoryPolicy.RELEASE ) );
        templates.add( new M2YumRepositoryTemplate( this, "maven2yum_hosted_snapshot", "Maven2 Yum (hosted, snapshot)",
            RepositoryPolicy.SNAPSHOT ) );
        templates.add( new M2YumGroupRepositoryTemplate( this, "maven2yum_group", "Maven2 Yum (group)" ) );

        return templates;
    }

}
