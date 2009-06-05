package org.sonatype.nexus.mock.pages;

import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.util.ThreadUtils;

import com.thoughtworks.selenium.Selenium;

public class LoginWindow extends Window {
    private TextField username;
    private TextField password;
    private Button loginButton;
    private MainPage mainPage;

    public LoginWindow(Selenium selenium, MainPage mainPage) {
        super(selenium, "window.Ext.getCmp('login-window')");
        this.mainPage = mainPage;

        username = new TextField(selenium, "window.Ext.getCmp('usernamefield')");
        password = new TextField(selenium, "window.Ext.getCmp('passwordfield')");
        loginButton = new Button(selenium, "window.Ext.getCmp('loginbutton')");
    }

    public LoginWindow populate(User user) {
        this.username.type(user.getUsername());
        this.password.type(user.getPassword());

        return this;
    }

    public LoginWindow login() {
        loginButton.click();

        return this;
    }

    public TextField getUsername() {
        return username;
    }

    public TextField getPassword() {
        return password;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public void loginExpectingSuccess() {
        login();
        waitForHidden();

        // wait for the login-link to change
        ThreadUtils.waitFor(new ThreadUtils.WaitCondition() {
            public boolean checkCondition(long elapsedTimeInMs) {
                return !mainPage.loginLinkAvailable();
            }
        }, TimeUnit.SECONDS, 15);
    }
}
