package dev.allstak;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AllStakFacadeTest {

    @AfterEach
    void tearDown() {
        AllStak.reset();
    }

    @Test
    void noOpBeforeInit() {
        // These should all silently return without error
        AllStak.captureException(new RuntimeException("test"));
        AllStak.captureLog("info", "test");
        AllStak.flush();
        assertThat(AllStak.isInitialized()).isFalse();
        assertThat(AllStak.getClient()).isNull();
    }

    @Test
    void initializesSuccessfully() {
        AllStakConfig config = AllStakConfig.builder()
                .apiKey("test-key")
                .debug(true)
                .build();

        AllStak.init(config);
        assertThat(AllStak.isInitialized()).isTrue();
        assertThat(AllStak.getClient()).isNotNull();
    }

    @Test
    void doubleInitIsIdempotent() {
        AllStakConfig config = AllStakConfig.builder()
                .apiKey("test-key")
                .build();

        AllStak.init(config);
        AllStakClient first = AllStak.getClient();

        AllStak.init(config); // second call — should be ignored
        assertThat(AllStak.getClient()).isSameAs(first);
    }

    @Test
    void shutdownClearsClient() {
        AllStakConfig config = AllStakConfig.builder()
                .apiKey("test-key")
                .build();

        AllStak.init(config);
        assertThat(AllStak.isInitialized()).isTrue();

        AllStak.shutdown();
        assertThat(AllStak.isInitialized()).isFalse();
    }

    @Test
    void startJobReturnsHandleEvenBeforeInit() {
        var handle = AllStak.startJob("test-job");
        assertThat(handle).isNotNull();
        assertThat(handle.getSlug()).isEqualTo("test-job");
    }
}
