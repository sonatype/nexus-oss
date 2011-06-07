package org.sonatype.nexus.test.utils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for {@link NexusRequest}
 */
public class NexusRequestMatchers {

    public static abstract class NexusRequestMatcher extends TypeSafeMatcher<NexusRequest> {

        @Override
        protected void describeMismatchSafely(NexusRequest item, Description mismatchDescription) {
            super.describeMismatchSafely(item, mismatchDescription);
            if (item.getStatus() != null) {
                mismatchDescription.appendText("\n============= Status =============\n").appendText(item.getStatus().toString());
            }
            if (item.getResponseText() != null) {
                mismatchDescription.appendText("\n============= Response Text =============\n").appendText(item.getResponseText());
            }
            if (item.getThrowable() != null) {
                mismatchDescription.appendText("\n============= Throwable =============\n").appendText(ExceptionUtils.getFullStackTrace(item.getThrowable()));
            }
        }
    }

    public static class RespondsWithStatusCode extends NexusRequestMatcher {

        private int expectedStatusCode;

        private RespondsWithStatusCode(int expectedStatusCode) {
            this.expectedStatusCode = expectedStatusCode;
        }

        @Override
        protected boolean matchesSafely(NexusRequest item) {
            return item.assertStatusCode(expectedStatusCode);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("response with status code ").appendValue(expectedStatusCode);
        }

    }

    public static class RespondsWithSuccess extends NexusRequestMatcher {

        @Override
        protected boolean matchesSafely(NexusRequest item) {
            return item.assertSuccess();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("response successful");
        }
    }

    @Factory
    public static <T> RespondsWithStatusCode respondsWithStatusCode(final int expectedStatusCode) {
        return new RespondsWithStatusCode(expectedStatusCode);
    }

    @Factory
    public static <T> RespondsWithSuccess respondsWithSuccess() {
        return new RespondsWithSuccess();
    }
}
