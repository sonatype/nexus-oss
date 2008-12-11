/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.registry.ContentClass;

public class DefaultNexusTest
    extends AbstractNexusTestCase
{
    private Nexus defaultNexus;

    public Nexus getDefaultNexus()
    {
        return defaultNexus;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (Nexus) lookup( Nexus.class );
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testRepositoryTemplates()
        throws IOException
    {
        CRepository template = new CRepository();
        template.setId( "aTemplate" );
        template.setName( "This is a longish name" );

        getDefaultNexus().createRepositoryTemplate( template );

        Collection<CRepository> templates = getDefaultNexus().listRepositoryTemplates();

        assertEquals( 5, templates.size() );

        CRepository t1 = getDefaultNexus().readRepositoryTemplate( "aTemplate" );

        assertEquals( "aTemplate", t1.getId() );
        assertEquals( "This is a longish name", t1.getName() );

        getDefaultNexus().deleteRepositoryTemplate( "aTemplate" );

        templates = getDefaultNexus().listRepositoryTemplates();

        assertEquals( 4, templates.size() );
    }

    public void testRepositoryShadowTemplates()
        throws IOException
    {
        CRepositoryShadow template = new CRepositoryShadow();
        template.setId( "aTemplate" );
        template.setName( "This is a longish name" );

        getDefaultNexus().createRepositoryShadowTemplate( template );

        Collection<CRepositoryShadow> templates = getDefaultNexus().listRepositoryShadowTemplates();

        assertEquals( 2, templates.size() );

        CRepositoryShadow t1 = getDefaultNexus().readRepositoryShadowTemplate( "aTemplate" );

        assertEquals( "aTemplate", t1.getId() );
        assertEquals( "This is a longish name", t1.getName() );

        getDefaultNexus().deleteRepositoryShadowTemplate( "aTemplate" );

        templates = getDefaultNexus().listRepositoryShadowTemplates();

        assertEquals( 1, templates.size() );
    }

    public void testRepositoryMixedTemplates()
        throws IOException
    {
        CRepository template = new CRepository();
        template.setId( "aTemplate" );
        template.setName( "This is a repo" );

        getDefaultNexus().createRepositoryTemplate( template );

        Collection<CRepository> templates = getDefaultNexus().listRepositoryTemplates();

        assertEquals( 5, templates.size() );

        CRepositoryShadow stemplate = new CRepositoryShadow();
        stemplate.setId( "aTemplate" );
        stemplate.setName( "This is a shadow" );

        getDefaultNexus().createRepositoryShadowTemplate( stemplate );

        Collection<CRepositoryShadow> stemplates = getDefaultNexus().listRepositoryShadowTemplates();

        assertEquals( 2, stemplates.size() );

        CRepositoryShadow st1 = getDefaultNexus().readRepositoryShadowTemplate( "aTemplate" );

        assertEquals( "aTemplate", st1.getId() );
        assertEquals( "This is a shadow", st1.getName() );

        getDefaultNexus().deleteRepositoryTemplate( "aTemplate" );

        templates = getDefaultNexus().listRepositoryTemplates();
        stemplates = getDefaultNexus().listRepositoryShadowTemplates();

        assertEquals( 4, templates.size() );
        assertEquals( 2, stemplates.size() );
    }

    public void testListRepositoryContentClasses()
        throws Exception
    {
        Map<String, ContentClass> plexusContentClasses = getContainer().lookupMap( ContentClass.class );

        Collection<ContentClass> contentClasses = getDefaultNexus().listRepositoryContentClasses();

        assertEquals( plexusContentClasses.size(), contentClasses.size() );

        for ( ContentClass cc : plexusContentClasses.values() )
        {
            assertTrue( contentClasses.contains( cc ) );
        }
    }

    public void testBounceNexus()
        throws Exception
    {
        getDefaultNexus().stopService();

        getDefaultNexus().startService();
    }
}
