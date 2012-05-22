package org.sonatype.nexus.plugin.settings.usertoken;

import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.nexus.plugin.settings.ClientConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugin.settings.DownloadSettingsTemplateMojo.END_EXPR;
import static org.sonatype.nexus.plugin.settings.DownloadSettingsTemplateMojo.START_EXPR;
import static org.sonatype.nexus.plugin.settings.usertoken.UserTokenTemplateInterpolatorCustomizer.ENCRYPTED_SUFFIX;
import static org.sonatype.nexus.plugin.settings.usertoken.UserTokenTemplateInterpolatorCustomizer.USER_TOKEN;
import static org.sonatype.nexus.plugin.settings.usertoken.UserTokenTemplateInterpolatorCustomizer.USER_TOKEN_NAME_CODE;
import static org.sonatype.nexus.plugin.settings.usertoken.UserTokenTemplateInterpolatorCustomizer.USER_TOKEN_PASS_CODE;

/**
 * Tests for {@link UserTokenTemplateInterpolatorCustomizer}.
 */
public class UserTokenTemplateInterpolatorCustomizerTest
    extends TestSupport
{
    @Mock
    private UserTokenClient userTokens;

    @Mock
    private MasterPasswordEncryption encryption;

    private UserTokenTemplateInterpolatorCustomizer customizer;

    private StringSearchInterpolator interpolator;

    @Before
    public void setUp() throws Exception {
        UserTokenDTO userToken = new UserTokenDTO();
        userToken.setNameCode("nc");
        userToken.setPassCode("pc");
        userToken.setCreated("c");

        when(userTokens.getCurrent(any(ClientConfiguration.class))).thenReturn(userToken);
        when(encryption.encrypt(any(String.class))).thenReturn("{foo}");

        customizer = new UserTokenTemplateInterpolatorCustomizer(userTokens, encryption);
        interpolator = new StringSearchInterpolator(START_EXPR, END_EXPR);

        ClientConfiguration config = mock(ClientConfiguration.class);
        customizer.customize(config, interpolator);
    }

    @Test
    public void interpolate_userToken() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN + "]");
        assertEquals("nc:pc", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
    }

    @Test
    public void interpolate_userToken_encrypted() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN + ENCRYPTED_SUFFIX + "]");
        assertEquals("{foo}", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
        verify(encryption, times(1)).encrypt(any(String.class));
    }

    @Test
    public void interpolate_userToken_nameCode() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN_NAME_CODE + "]");
        assertEquals("nc", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
    }

    @Test
    public void interpolate_userToken_nameCode_encrypted() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN_NAME_CODE + ENCRYPTED_SUFFIX + "]");
        assertEquals("{foo}", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
        verify(encryption, times(1)).encrypt(any(String.class));
    }

    @Test
    public void interpolate_userToken_passCode() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN_PASS_CODE + "]");
        assertEquals("pc", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
    }

    @Test
    public void interpolate_userToken_passCode_encrypted() throws Exception {
        String result = interpolator.interpolate("$[" + USER_TOKEN_PASS_CODE + ENCRYPTED_SUFFIX + "]");
        assertEquals("{foo}", result);
        verify(userTokens, times(1)).getCurrent(any(ClientConfiguration.class));
        verify(encryption, times(1)).encrypt(any(String.class));
    }
}
