/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.tools;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * Default implementation of the ConcurrentConfigurationManager interface. Intended to
 * provide a way to access the ConfigurationManager in a thread-safe manner.
 * 
 * This implementation simply wraps a ConfigurationManager instance, providing an interface
 * for users to use it in a thread-safe manner
 * 
 * @author Steve Carlucci
 *
 */
@Singleton
@Typed( ConcurrentConfigurationManager.class )
@Named( "default" )
public class DefaultConcurrentConfigurationManager implements ConcurrentConfigurationManager
{
    private final ConfigurationManager configurationManager;
    
    private final ReadWriteLock readWriteLock;
    
    private final Lock readLock;
    
    private final Lock writeLock;
    
    private final Map<Long, ConfigurationManagerActionType> threadLocks;
    
    @Inject
    public DefaultConcurrentConfigurationManager(@Named("resourceMerging") ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
        
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
        this.threadLocks = new ConcurrentHashMap<Long, ConfigurationManagerActionType>();
    }
    
    @Override
    public <X1 extends Exception, X2 extends Exception> void run(ConfigurationManagerAction action) throws X1, X2
    {
        Lock lock = this.acquireLock(action.getActionType());
        try
        {
            action.<X1, X2>run();
        }
        finally
        {
            this.releaseLock(lock);
        }
    }

    @Override
    public void deleteUserRoleMapping(String userId, String source)
        throws NoSuchRoleMappingException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.deleteUserRoleMapping(userId, source);
    }

    @Override
    public void deleteUser(String id)
        throws UserNotFoundException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.deleteUser(id);
    }

    @Override
    public void deleteRole(String id)
        throws NoSuchRoleException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.deleteRole(id);
    }

    @Override
    public void deletePrivilege(String id)
        throws NoSuchPrivilegeException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.deletePrivilege(id);
    }

    @Override
    public String getPrivilegeProperty(String id, String key)
        throws NoSuchPrivilegeException
    {
        readLock.lock();
        try
        {
            return configurationManager.getPrivilegeProperty(id, key);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void clearCache()
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.clearCache();
    }

    @Override
    public void save()
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.save();
    }

    @Override
    public void cleanRemovedRole(String roleId)
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.cleanRemovedRole(roleId);
    }

    @Override
    public void cleanRemovedPrivilege(String privilegeId)
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.cleanRemovedPrivilege(privilegeId);
    }

    @Override
    public List<CUser> listUsers()
    {
        readLock.lock();
        try
        {
            return configurationManager.listUsers();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public List<CRole> listRoles()
    {
        readLock.lock();
        try
        {
            return configurationManager.listRoles();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public List<CPrivilege> listPrivileges()
    {
        readLock.lock();
        try
        {
            return configurationManager.listPrivileges();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public List<PrivilegeDescriptor> listPrivilegeDescriptors()
    {
        readLock.lock();
        try
        {
            return configurationManager.listPrivilegeDescriptors();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void createUser(CUser user, Set<String> roles)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUser(user, roles);
    }

    @Override
    public void createUser(CUser user, String password, Set<String> roles)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUser(user, password, roles);
    }

    @Override
    public void createUser(CUser user, Set<String> roles, SecurityValidationContext context)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUser(user, roles, context);
    }

    @Override
    public void createUser(CUser user, String password, Set<String> roles, SecurityValidationContext context)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUser(user, password, roles, context);
    }

    @Override
    public void createRole(CRole role)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createRole(role);
    }

    @Override
    public void createRole(CRole role, SecurityValidationContext context)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createRole(role, context);
    }

    @Override
    public void createPrivilege(CPrivilege privilege)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createPrivilege(privilege);
    }

    @Override
    public void createPrivilege(CPrivilege privilege, SecurityValidationContext context)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createPrivilege(privilege, context);
    }

    @Override
    public CUser readUser(String id)
        throws UserNotFoundException
    {
        readLock.lock();
        try
        {
            return configurationManager.readUser(id);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public CRole readRole(String id)
        throws NoSuchRoleException
    {
        readLock.lock();
        try
        {
            return configurationManager.readRole(id);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public CPrivilege readPrivilege(String id)
        throws NoSuchPrivilegeException
    {
        readLock.lock();
        try
        {
            return configurationManager.readPrivilege(id);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void updateUser(CUser user, Set<String> roles)
        throws InvalidConfigurationException, UserNotFoundException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateUser(user, roles);
    }

    @Override
    public void updateUser(CUser user, Set<String> roles, SecurityValidationContext context)
        throws InvalidConfigurationException, UserNotFoundException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateUser(user, roles, context);
    }

    @Override
    public void updateRole(CRole role)
        throws InvalidConfigurationException, NoSuchRoleException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateRole(role);
    }

    @Override
    public void updateRole(CRole role, SecurityValidationContext context)
        throws InvalidConfigurationException, NoSuchRoleException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateRole(role, context);
    }

    @Override
    public void createUserRoleMapping(CUserRoleMapping userRoleMapping)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUserRoleMapping(userRoleMapping);
    }

    @Override
    public void createUserRoleMapping(CUserRoleMapping userRoleMapping, SecurityValidationContext context)
        throws InvalidConfigurationException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.createUserRoleMapping(userRoleMapping, context);
    }

    @Override
    public void updateUserRoleMapping(CUserRoleMapping userRoleMapping)
        throws InvalidConfigurationException, NoSuchRoleMappingException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateUserRoleMapping(userRoleMapping);
    }

    @Override
    public void updateUserRoleMapping(CUserRoleMapping userRoleMapping, SecurityValidationContext context)
        throws InvalidConfigurationException, NoSuchRoleMappingException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updateUserRoleMapping(userRoleMapping, context);
    }

    @Override
    public CUserRoleMapping readUserRoleMapping(String userId, String source)
        throws NoSuchRoleMappingException
    {
        readLock.lock();
        try
        {
            return configurationManager.readUserRoleMapping(userId, source);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public List<CUserRoleMapping> listUserRoleMappings()
    {
        readLock.lock();
        try
        {
            return configurationManager.listUserRoleMappings();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void updatePrivilege(CPrivilege privilege)
        throws InvalidConfigurationException, NoSuchPrivilegeException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updatePrivilege(privilege);
    }

    @Override
    public void updatePrivilege(CPrivilege privilege, SecurityValidationContext context)
        throws InvalidConfigurationException, NoSuchPrivilegeException
    {
        checkLock(ConfigurationManagerActionType.WRITE);
        configurationManager.updatePrivilege(privilege, context);
    }

    @Override
    public String getPrivilegeProperty(CPrivilege privilege, String key)
    {
        readLock.lock();
        try
        {
            return configurationManager.getPrivilegeProperty(privilege, key);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public SecurityValidationContext initializeContext()
    {
        readLock.lock();
        try
        {
            return configurationManager.initializeContext();
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * Acquire appropriate lock based on provided action type, and track
     * that the currently executing thread has acquired this lock
     * 
     * @param actionType lock type to acquire
     * @return the acquired lock
     */
    private Lock acquireLock(ConfigurationManagerActionType actionType)
    {
        Lock lock = actionType == ConfigurationManagerActionType.READ ? this.readLock : this.writeLock;
        lock.lock();
        
        try
        {
            //Track the type of lock that this thread has acquired
            this.threadLocks.put(Thread.currentThread().getId(), actionType);
        }
        catch(RuntimeException e)
        {
            lock.unlock();
            throw e;
        }
        
        return lock;
    }
    
    /**
     * Release specified lock, and remove thread-specific lock record
     * @param lock lock to unlock
     */
    private void releaseLock(Lock lock)
    {
        try
        {
            //Remove thread from our tracking map
            this.threadLocks.remove(Thread.currentThread().getId());
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * Checks that the currently executing thread has the appropriate lock
     * 
     * @param actionType the type of lock to check (e.g. read/write)
     * @throws UnsupportedOperationException if thread does not have appropriate lock
     */
    private void checkLock(ConfigurationManagerActionType actionType)
    {
        ConfigurationManagerActionType type = this.threadLocks.get(Thread.currentThread().getId());
        if(type == null || (actionType == ConfigurationManagerActionType.WRITE && type == ConfigurationManagerActionType.READ) )
        {
            throw new UnsupportedOperationException("Method called without proper locking");
        }
    }

}
