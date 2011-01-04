/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.templates.repository.maven;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.templates.TemplateSet;

public class MavenRepositoryTemplateTest
    extends AbstractNexusTestCase
{
    private Nexus nexus;

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexus = lookup( Nexus.class );
    }

    protected Nexus getNexus()
    {
        return nexus;
    }

    public void testAvailableRepositoryTemplateCount()
        throws Exception
    {
        TemplateSet templates = getNexus().getRepositoryTemplates();

        assertEquals( "Template count is wrong!", 12, templates.size() );
    }

    public void testSimpleSelection()
        throws Exception
    {
        TemplateSet groups = getNexus().getRepositoryTemplates().getTemplates( MavenGroupRepository.class );

        assertEquals( "Template count is wrong!", 2, groups.size() );

        assertEquals( "Template count is wrong!", 1, groups.getTemplates( new Maven1ContentClass() ).size() );
        assertEquals( "Template count is wrong!", 1, groups.getTemplates( Maven1ContentClass.class ).size() );

        assertEquals( "Template count is wrong!", 1, groups.getTemplates( new Maven2ContentClass() ).size() );
        assertEquals( "Template count is wrong!", 1, groups.getTemplates( Maven2ContentClass.class ).size() );
    }
}
