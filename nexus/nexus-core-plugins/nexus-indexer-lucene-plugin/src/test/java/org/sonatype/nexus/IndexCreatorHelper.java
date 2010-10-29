/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 *  Creators that can be used in tests.
 */
public class IndexCreatorHelper
{
    private List<IndexCreator> m_fullCreators = new ArrayList<IndexCreator>();
    private List<IndexCreator> m_defaultCreators =  new ArrayList<IndexCreator>();
    private List<IndexCreator> m_minCreators =  new ArrayList<IndexCreator>();

    public IndexCreatorHelper( PlexusContainer testCaseContainer )
        throws ComponentLookupException
    {
          IndexCreator min = testCaseContainer.lookup( IndexCreator.class, "min" );
        IndexCreator jar = testCaseContainer.lookup( IndexCreator.class, "jarContent" );

        m_minCreators.add( min );

        m_fullCreators.add( min );
        m_fullCreators.add( jar );

        m_defaultCreators.addAll( m_fullCreators );
    }

    public List<IndexCreator> getDefaultCreators() {
        return m_defaultCreators;
    }

    public List<IndexCreator> getFullCreators() {
        return m_fullCreators;
    }

    public List<IndexCreator> getMinCreators() {
        return m_minCreators;
    }
}
