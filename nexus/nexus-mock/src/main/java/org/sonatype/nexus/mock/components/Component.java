package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;
import org.sonatype.nexus.mock.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

public class Component {
    private Component parent;
    protected Selenium selenium;
    private String expression;

    public Component(Selenium selenium) {
        this.selenium = selenium;
    }

    /**
     * Makes a proxy for a root Ext component; selenium - Selenium proxy through which it can fire Selenese commands,
     * expression - JavaScript that evaluates to the Ext component.
     */
    public Component(Selenium selenium, String expression) {
        this.parent = null;
        this.selenium = selenium;
        this.expression = expression;
    }

    /**
     * Makes a proxy for an Ext component that is contained within another; parent - proxy for the container Ext
     * component, expression - JavaScript expression that evaluates this proxy's component on that of the container.
     */
    public Component(Component parent, String expression) {
        this.parent = parent;
        this.selenium = parent.selenium;
        this.expression = expression;
    }

    /**
     * Returns the ID of the Ext component, found with the proxy's JS expression. This is overridden in some subclasses
     * for where the expression to get the ID varies.
     */
    public String getId() {
        return selenium.getEval(this.getExpression() + ".getId()");
    }

    /**
     * Returns an XPath to the Ext component, which contains the ID provided by getId()
     */
    public String getXPath() {
        return "//*[@id='" + getId() + "']";
    }

    /**
     * Returns the absolute expression that resolves this proxy's Ext component.
     */
    public String getExpression() {
        return (parent != null) ? parent.getExpression() + expression : expression;
    }

    protected void waitForEvalTrue(String expr) {
        String fullExpr = getExpression() + expr;

        for (int second = 0; ; second++) {
            if (second >= 15) {
                throw new RuntimeException("Timeout");
            }

            try {
                if ("true".equals(selenium.getEval(fullExpr))) {
                    break;
                }
            } catch (Exception e) {
                // ignore
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    protected boolean evalTrue(String expr) {
        String fullExpr = expression + expr;

        try {
            return "true".equals(selenium.getEval(fullExpr));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hidden() {
        return evalTrue(" == null") || evalTrue(".hidden");
    }

    public boolean visible() {
        return !hidden();
    }

    public void waitForHidden() {
        boolean success = ThreadUtils.waitFor(new ThreadUtils.WaitCondition() {
            @Override
            public boolean checkCondition(long elapsedTimeInMs) {
                return hidden();
            }
        }, TimeUnit.SECONDS, 15);

        if (!success) {
            throw new RuntimeException("Timeout");
        }
    }

    public void waitForVisible() {
        boolean success = ThreadUtils.waitFor(new ThreadUtils.WaitCondition() {
            @Override
            public boolean checkCondition(long elapsedTimeInMs) {
                return visible();
            }
        }, TimeUnit.SECONDS, 15);

        if (!success) {
            throw new RuntimeException("Timeout");
        }
    }
}
