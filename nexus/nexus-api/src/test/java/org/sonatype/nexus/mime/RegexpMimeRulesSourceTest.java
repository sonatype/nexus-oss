/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.mime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * Testing RegexpMimeRulesSource, a handy class for it's wanted behavior. While everyone is free to reimplement the
 * MimeRulesSource as they want, this class is just a general utility since probably it fits almost always.
 */
public class RegexpMimeRulesSourceTest
{
    @Test
    public void testRegectMimeRulesSourceTest()
    {
        final RegexpMimeRulesSource mimeRulesSource = new RegexpMimeRulesSource();

        mimeRulesSource.addRule( ".*\\.foo\\z", "foo/bar" );
        mimeRulesSource.addRule( ".*\\.pom\\z", "application/x-pom" );
        mimeRulesSource.addRule( "(.*/maven-metadata.xml\\z)|(maven-metadata.xml\\z)", "application/x-maven-metadata" );

        // "more specific" one
        mimeRulesSource.addRule( "\\A/atom-service/.*\\.xml\\z", "application/atom+xml" );
        // and now the "general one"
        mimeRulesSource.addRule( ".*\\.xml\\z", "application/xml" );

        assertThat( mimeRulesSource.getRuleForPath( "/some/repo/path/content.foo" ), equalTo( "foo/bar" ) );
        assertThat( mimeRulesSource.getRuleForPath( "/some/repo/path/content.foo.bar" ), nullValue() );
        assertThat( mimeRulesSource.getRuleForPath( "/log4j/log4j/1.2.12/log4j-1.2.12.pom" ),
            equalTo( "application/x-pom" ) );
        assertThat( mimeRulesSource.getRuleForPath( "maven-metadata.xml" ), equalTo( "application/x-maven-metadata" ) );
        assertThat( mimeRulesSource.getRuleForPath( "/maven-metadata.xml" ), equalTo( "application/x-maven-metadata" ) );
        assertThat( mimeRulesSource.getRuleForPath( "/org/sonatype/nexus/maven-metadata.xml" ),
            equalTo( "application/x-maven-metadata" ) );
        assertThat( mimeRulesSource.getRuleForPath( "/org/sonatype/nexus/maven-metadata.xml.bar" ), nullValue() );
        assertThat( mimeRulesSource.getRuleForPath( "/org/sonatype/nexus/maven-metadata.xml/tricky/path.pom" ),
            equalTo( "application/x-pom" ) );

        assertThat( mimeRulesSource.getRuleForPath( "/org/sonatype/nexus/maven-metadata1.xml" ),
            equalTo( "application/xml" ) );
        assertThat( mimeRulesSource.getRuleForPath( "/atom-service//org/sonatype/nexus/maven-metadata1.xml" ),
            equalTo( "application/atom+xml" ) );

    }

}
