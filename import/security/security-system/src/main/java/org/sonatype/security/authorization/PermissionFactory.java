/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.authorization;

import org.apache.shiro.authz.Permission;

/**
 * A permission factory that creates Permission instances. It may apply other stuff, like caching instances for example,
 * based on permission string representation. This is just a concept to be able to hide caching of it. Which
 * implementation you use, depends on your app very much, but usually you'd want the one producing the most widely used
 * permission in Shiro: the {@link WildcardPermissionFactory}.
 * 
 * @author cstamas
 * @since 2.8
 */
public interface PermissionFactory
{
    Permission create( final String permission );
}
