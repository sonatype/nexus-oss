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
package org.sonatype.security.realms.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.shiro.authc.credential.PasswordService;
import org.junit.Test;
import org.sonatype.guice.bean.containers.InjectedTestCase;

public class LegacyNexusPasswordServiceTest
    extends InjectedTestCase
{
    LegacyNexusPasswordService passwordService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.passwordService = (LegacyNexusPasswordService) lookup(PasswordService.class, "legacy");
    }
    
    @Test
    public void testSha1Hash()
    {
        String password = "admin123";
        String sha1Hash = "f865b53623b121fd34ee5426c792e5c33af8c227";
        
        assertThat(this.passwordService.passwordsMatch(password, sha1Hash), is(true));
    }
    
    @Test
    public void testMd5Hash()
    {
        String password = "admin123";
        String md5Hash = "0192023a7bbd73250516f069df18b500";
        
        assertThat(this.passwordService.passwordsMatch(password, md5Hash), is(true));
    }
    
    @Test
    public void testInvalidSha1Hash()
    {
        String password = "admin123";
        String sha1Hash = "f865b53623b121fd34ee5426c792e5c33af8c228";
        
        assertThat(this.passwordService.passwordsMatch(password, sha1Hash), is(false));
    }
    
    @Test
    public void testInvalidMd5Hash()
    {
        String password = "admin123";
        String md5Hash = "0192023a7bbd73250516f069df18b501";
        
        assertThat(this.passwordService.passwordsMatch(password, md5Hash), is(false));
    }
}
