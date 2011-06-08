package org.sonatype.nexus.test.utils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;
import org.restlet.data.Response;
import org.restlet.data.Status;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
/**
 * Matchers for {@link NexusRequest}
 */
public class NexusRequestMatchers {

    // **************** Status Matchers ********************


    public static abstract class BaseStatusMatcher extends TypeSafeMatcher<Status>{
        @Override
        protected void describeMismatchSafely(Status status, Description mismatchDescription) {
            mismatchDescription.appendText("was ").appendText(status.toString());
        }
    }

    public static class HasCode extends BaseStatusMatcher {
        private int expectedCode;
        private HasCode(final int expectedCode){
            this.expectedCode = expectedCode;
        }

        @Override
        protected boolean matchesSafely(Status item) {
            return item.getCode() == expectedCode;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("status code of ").appendValue(this.expectedCode);
        }
    }

    public static class IsSuccess extends BaseStatusMatcher {

        @Override
        protected boolean matchesSafely(Status item) {
            return item.isSuccess();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("success");
        }
    }

    public static class IsError extends BaseStatusMatcher {

        @Override
        protected boolean matchesSafely(Status item) {
            return item.isError();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("error");
        }
    }


    // **************** Response Matchers ******************

    public static abstract class BaseResponseMatcher extends TypeSafeMatcher<Response>{
        @Override
        protected void describeMismatchSafely(Response item, Description mismatchDescription) {
            mismatchDescription.appendText(item.getStatus().toString());
        }
    }

    public static class RespondsWithStatusCode extends BaseResponseMatcher {

        private int expectedStatusCode;

        private RespondsWithStatusCode(int expectedStatusCode) {
            this.expectedStatusCode = expectedStatusCode;
        }

        @Override
        protected boolean matchesSafely(Response item) {
            return item.getStatus().getCode() == expectedStatusCode;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("response successful");
        }
    }


    public static class InError extends BaseResponseMatcher {

        @Override
        protected boolean matchesSafely(Response resp) {
            return resp.getStatus().isError();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("response status in error");
        }

    }

    public static class IsSuccessful extends BaseResponseMatcher {

        @Override
        protected boolean matchesSafely(Response resp) {
            return resp.getStatus().isSuccess();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("response status to indicate success status 200");
        }

    }



    @Factory
    public static <T> IsSuccess isSuccess() {
        return new IsSuccess();
    }

    @Factory
    public static <T> IsError isError() {
        return new IsError();
    }


    @Factory
    public static <T> HasCode hasStatusCode(int expectedCode) {
        return new HasCode(expectedCode);
    }

    @Factory
    public static <T> RespondsWithStatusCode respondsWithStatusCode(final int expectedStatusCode) {
        return new RespondsWithStatusCode(expectedStatusCode);
    }

    @Factory
    public static <T> InError inError() {
        return new InError();
    }

    @Factory
    public static <T> IsSuccessful isSuccessful() {
        return new IsSuccessful();
    }




}
