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
package org.sonatype.security.usermanagement;

import java.util.HashSet;
import java.util.Set;

/**
 * A UserSearchCriteria defines searchble fields. Null or empty fields will be ignored.
 * 
 * @author Brian Demers
 */
public class UserSearchCriteria
{
    private String userId;

    private Set<String> oneOfRoleIds = new HashSet<String>();

    private String source;

    private String email;

    public UserSearchCriteria()
    {
    }

    public UserSearchCriteria( String userId )
    {
        this.userId = userId;
    }

    public UserSearchCriteria( String userId, Set<String> oneOfRoleIds, String source )
    {
        this.userId = userId;
        this.oneOfRoleIds = oneOfRoleIds;
        this.source = source;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public Set<String> getOneOfRoleIds()
    {
        return oneOfRoleIds;
    }

    public void setOneOfRoleIds( Set<String> oneOfRoleIds )
    {
        this.oneOfRoleIds = oneOfRoleIds;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

}
