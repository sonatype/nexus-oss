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
import org.apache.shiro.crypto.hash.Hash;
import org.junit.Test;
import org.sonatype.security.AbstractSecurityTestCase;

public class DefaultSecurityPasswordServiceTest
    extends AbstractSecurityTestCase
{
    DefaultSecurityPasswordService passwordService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.passwordService = (DefaultSecurityPasswordService) lookup(PasswordService.class, "default");
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
    public void testShiro1HashFormat()
    {
        String password = "admin123";
        String shiro1Hash = "$shiro1$SHA-512$1024$zjU1u+Zg9UNwuB+HEawvtA==$IzF/OWzJxrqvB5FCe/2+UcZhhZYM2pTu0TEz7Ybnk65AbbEdUk9ntdtBzkN8P3gZby2qz6MHKqAe8Cjai9c4Gg==";
        
        assertThat(this.passwordService.passwordsMatch(password, shiro1Hash), is(true));
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
    
    @Test
    public void testInvalidShiro1HashFormat()
    {
        String password = "admin123";
        String shiro1Hash = "$shiro1$SHA-512$1024$zjU1u+Zg9UNwuB+HEawvtA==$IzF/OWzjxrqvB5FCe/2+UcZhhZYM2pTu0TEz7Ybnk65AbbEdUk9ntdtBzkN8P3gZby2qz6MHKqAe8Cjai9c4Gg==";
        
        assertThat(this.passwordService.passwordsMatch(password, shiro1Hash), is(false));
    }
    
    @Test
    public void testHash()
    {
        String password = "testpassword";
        Hash hash = this.passwordService.hashPassword(password);
        
        assertThat(this.passwordService.passwordsMatch(password, hash), is(true));
    }
}
