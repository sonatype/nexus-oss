/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.client.internal.rest.jersey.subsystem.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import javax.annotation.Nullable;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.security.User;
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceResponse;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Jersey based {@link Users} implementation.
 *
 * @since 2.3
 */
public class JerseyUsers
    extends SubsystemSupport<JerseyNexusClient>
    implements Users
{

  public JerseyUsers(final JerseyNexusClient nexusClient) {
    super(nexusClient);
  }

  @Override
  public JerseyUser create(final String id) {
    return new JerseyUser(getNexusClient(), id);
  }

  @Override
  public User get(final String id) {
    try {
      return convert(
          getNexusClient()
              .serviceResource(path(id))
              .get(UserResourceResponse.class)
              .getData()
      );
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }
  }

  @Override
  public Collection<User> get() {
    final UserListResourceResponse users;
    try {
      users = getNexusClient()
          .serviceResource("users")
          .get(UserListResourceResponse.class);
    }
    catch (UniformInterfaceException e) {
      throw getNexusClient().convert(e);
    }
    catch (ClientHandlerException e) {
      throw getNexusClient().convert(e);
    }

    return Collections2.transform(users.getData(), new Function<UserResource, User>()
    {
      @Override
      public User apply(@Nullable final UserResource input) {
        return convert(input);
      }
    });
  }

  private JerseyUser convert(@Nullable final UserResource resource) {
    if (resource == null) {
      return null;
    }
    final JerseyUser role = new JerseyUser(getNexusClient(), resource);
    role.overwriteWith(resource);
    return role;
  }

  static String path(final String id) {
    try {
      return "users/" + URLEncoder.encode(id, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

}
