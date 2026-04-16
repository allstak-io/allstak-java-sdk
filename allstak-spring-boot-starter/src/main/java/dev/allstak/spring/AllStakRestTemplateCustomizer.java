package dev.allstak.spring;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-applied {@link RestTemplateCustomizer} that attaches the AllStak
 * outbound HTTP interceptor to any {@link RestTemplate} produced through
 * {@link org.springframework.boot.web.client.RestTemplateBuilder}.
 *
 * <p>Spring Boot's {@code RestTemplateBuilder} automatically picks up every
 * {@code RestTemplateCustomizer} bean in the context and applies it on
 * {@code .build()}. This means any user code like
 * <pre>{@code
 * @Bean
 * RestTemplate restTemplate(RestTemplateBuilder builder) {
 *     return builder.build();
 * }
 * }</pre>
 * automatically gets AllStak outbound HTTP capture — zero extra code.
 *
 * <p>The {@link AllStakRestTemplatePostProcessor} handles the other path
 * (RestTemplates created with {@code new RestTemplate()}). Together they
 * cover every common way of obtaining a RestTemplate in Spring Boot.
 *
 * <p>Duplicate-interceptor guard: if the interceptor is already present on
 * the RestTemplate (for any reason), we do not add it again.
 */
public class AllStakRestTemplateCustomizer implements RestTemplateCustomizer {

    private final AllStakRestTemplateInterceptor interceptor;

    public AllStakRestTemplateCustomizer(AllStakRestTemplateInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        for (ClientHttpRequestInterceptor existing : restTemplate.getInterceptors()) {
            if (existing instanceof AllStakRestTemplateInterceptor) {
                return;
            }
        }
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
    }
}
