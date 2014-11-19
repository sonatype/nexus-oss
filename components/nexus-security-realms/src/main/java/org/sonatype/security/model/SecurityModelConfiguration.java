package org.sonatype.security.model;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: adreghiciu
 * Date: 19/11/14
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public interface SecurityModelConfiguration
    extends org.sonatype.configuration.Configuration, java.io.Serializable, Cloneable
{
  List<CUser> getUsers();

  CUser getUser(String id);

  void addUser(CUser user);

  void setUsers(Collection<CUser> users);

  boolean removeUser(String id);

  List<CUserRoleMapping> getUserRoleMappings();

  CUserRoleMapping getUserRoleMapping(String userId, String source);

  void addUserRoleMapping(CUserRoleMapping mapping);

  void setUserRoleMappings(Collection<CUserRoleMapping> mappings);

  boolean removeUserRoleMapping(String userId, String source);

  List<CPrivilege> getPrivileges();

  CPrivilege getPrivilege(String id);

  void addPrivilege(CPrivilege privilege);

  void setPrivileges(Collection<CPrivilege> privileges);

  boolean removePrivilege(String id);

  List<CRole> getRoles();

  CRole getRole(String id);

  void addRole(CRole role);

  void setRoles(Collection<CRole> roles);

  boolean removeRole(String id);
}
