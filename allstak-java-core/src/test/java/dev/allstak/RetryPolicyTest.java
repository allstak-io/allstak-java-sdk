package dev.allstak;

import dev.allstak.transport.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void maxAttemptsIsFive() {
        assertThat(RetryPolicy.maxAttempts()).isEqualTo(5);
    }

    @Test
    void firstAttemptIsImmediate() {
        assertThat(RetryPolicy.delayForAttempt(0)).isEqualTo(0);
    }

    @Test
    void subsequentAttemptsHaveBackoff() {
        // Attempt 1: ~1000-1500ms
        long delay1 = RetryPolicy.delayForAttempt(1);
        assertThat(delay1).isBetween(1000L, 1500L);

        // Attempt 2: ~2000-2500ms
        long delay2 = RetryPolicy.delayForAttempt(2);
        assertThat(delay2).isBetween(2000L, 2500L);

        // Attempt 3: ~4000-4500ms
        long delay3 = RetryPolicy.delayForAttempt(3);
        assertThat(delay3).isBetween(4000L, 4500L);

        // Attempt 4: ~8000-8500ms
        long delay4 = RetryPolicy.delayForAttempt(4);
        assertThat(delay4).isBetween(8000L, 8500L);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void serverErrorsAreRetryable(int status) {
        assertThat(RetryPolicy.isRetryable(status)).isTrue();
    }

    @Test
    void tooManyRequestsIsRetryable() {
        assertThat(RetryPolicy.isRetryable(429)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 422})
    void clientErrorsAreNotRetryable(int status) {
        assertThat(RetryPolicy.isRetryable(status)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 422})
    void clientErrorsDetected(int status) {
        assertThat(RetryPolicy.isClientError(status)).isTrue();
    }

    @Test
    void authErrorDetected() {
        assertThat(RetryPolicy.isAuthError(401)).isTrue();
        assertThat(RetryPolicy.isAuthError(403)).isFalse();
        assertThat(RetryPolicy.isAuthError(200)).isFalse();
    }
}
