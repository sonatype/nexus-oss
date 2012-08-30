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
package org.sonatype.nexus.maven.site.plugin;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractWebSiteRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * The default Maven Site Repository.
 * 
 * @author cstamas
 */
@Component( role = WebSiteRepository.class, hint = "maven-site", instantiationStrategy = "per-lookup", description = "Maven Site Repository" )
public class DefaultMavenSiteRepository
    extends AbstractWebSiteRepository
    implements MavenSiteRepository, WebSiteRepository
{
    @Requirement( hint = "maven-site" )
    private ContentClass contentClass;

    @Requirement
    private DefaultMavenSiteRepositoryConfigurator repositoryConfigurator;

    private RepositoryKind repositoryKind;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind = new DefaultRepositoryKind( MavenSiteRepository.class, null );
        }

        return repositoryKind;
    }

    public void deploySiteBundle( String prefix, InputStream bundle )
        throws IOException
    {
        throw new UnsupportedOperationException( "Deploy of the bundle is not yet implemented!" );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<DefaultMavenSiteRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultMavenSiteRepositoryConfiguration>()
        {
            public DefaultMavenSiteRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultMavenSiteRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    @Override
    public Configurator getConfigurator()
    {
        return repositoryConfigurator;
    }

    @Override
    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        // strip the '.' from the path
        if ( AbstractStorageItem.class.isAssignableFrom( item.getClass() ) )
        {
            String normalizedPath = FileUtils.normalize( item.getPath() );
            AbstractStorageItem fileItem = (AbstractStorageItem) item;
            fileItem.setPath( normalizedPath );
        }

        super.storeItem( fromTask, item );
    }
}
