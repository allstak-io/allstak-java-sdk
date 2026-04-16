package dev.allstak.spring;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-applied {@link WebClientCustomizer} that attaches the AllStak
 * {@link AllStakWebClientFilter} to any {@link WebClient} produced through
 * {@link WebClient.Builder}.
 *
 * <p>Spring Boot's {@code WebClient.Builder} bean automatically applies
 * every {@code WebClientCustomizer} in the context when {@code .build()} is
 * called, so user code like
 * <pre>{@code
 * @Bean
 * WebClient webClient(WebClient.Builder builder) {
 *     return builder.baseUrl("https://api.example.com").build();
 * }
 * }</pre>
 * automatically gets AllStak outbound capture — zero extra code.
 *
 * <p>This does <b>not</b> cover {@code WebClient.create()} /
 * {@code WebClient.create(url)} which bypass the builder bean entirely; those
 * must be instrumented manually by passing the filter via
 * {@code WebClient.builder().filter(filter).build()}. 99 % of Spring Boot
 * applications use the injected builder.
 */
public class AllStakWebClientCustomizer implements WebClientCustomizer {

    private final AllStakWebClientFilter filter;

    public AllStakWebClientCustomizer(AllStakWebClientFilter filter) {
        this.filter = filter;
    }

    @Override
    public void customize(WebClient.Builder builder) {
        builder.filter(filter);
    }
}
