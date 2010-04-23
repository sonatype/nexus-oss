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
package org.sonatype.nexus.configuration.application;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

public interface MutableConfiguration
{
    // ----------------------------------------------------------------------------------------------------------
    // Security (TODO: this should be removed, security has to be completely "paralell" and not interleaved!)
    // ----------------------------------------------------------------------------------------------------------

    boolean isSecurityEnabled();

    void setSecurityEnabled( boolean enabled )
        throws IOException;

    boolean isAnonymousAccessEnabled();

    void setAnonymousAccessEnabled( boolean enabled )
        throws IOException;

    String getAnonymousUsername();

    void setAnonymousUsername( String val )
        throws InvalidConfigurationException;

    String getAnonymousPassword();

    void setAnonymousPassword( String val )
        throws InvalidConfigurationException;

    List<String> getRealms();

    void setRealms( List<String> realms )
        throws InvalidConfigurationException;

    // ----------------------------------------------------------------------------
    // Scheduled Tasks
    // ----------------------------------------------------------------------------

    List<ScheduledTaskDescriptor> listScheduledTaskDescriptors();

    ScheduledTaskDescriptor getScheduledTaskDescriptor( String id );

    // ----------------------------------------------------------------------------------------------------------
    // Repositories
    // ----------------------------------------------------------------------------------------------------------
    
    /**
     * Sets the default (applied to all that has no exceptions set with {
     * {@link #setRepositoryMaxInstanceCount(RepositoryTypeDescriptor, int)} method) maxInstanceCount. Any positive
     * integer limits the max count of live instances, any less then 0 integer removes the limitation. Note: setting
     * limitations on already booted instance will not "enforce" the limitation!
     * 
     * @param count
     */
    void setDefaultRepositoryMaxInstanceCount( int count );

    /**
     * Limits the maxInstanceCount for the passed in repository type. Any positive integer limits the max count of live
     * instances, any less then 0 integer removes the limitation. Note: setting limitations on already booted instance
     * will not "enforce" the limitation!
     * 
     * @param rtd
     * @param count
     */
    void setRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd, int count );

    /**
     * Returns the count limit for the passed in repository type.
     * 
     * @param rtd
     * @return
     */
    int getRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd );

    // CRepository: CRUD

    /**
     * Creates a repository live instance out of the passed in model. It validates, registers it with repository
     * registry and puts it into configuration. And finally saves configuration.
     * 
     * @return the repository instance.
     * @throws ConfigurationException
     * @throws IOException
     */
    Repository createRepository( CRepository settings )
        throws ConfigurationException, IOException;

    /**
     * Removes repository from configuration, checks it's dependants too (ie shadows), updates groups and path mappings
     * (Routes on UI) if needed.
     * 
     * @param id
     * @throws NoSuchRepositoryException
     * @throws IOException
     * @throws ConfigurationException
     */
    void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException;

    // FIXME: This will be removed: NEXUS-2363 vvvvv
    // CRemoteNexusInstance

    Collection<CRemoteNexusInstance> listRemoteNexusInstances();

    CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException;

    void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException;

    void deleteRemoteNexusInstance( String alias )
        throws IOException;
    // FIXME: This will be removed: NEXUS-2363 ^^^^^

}
