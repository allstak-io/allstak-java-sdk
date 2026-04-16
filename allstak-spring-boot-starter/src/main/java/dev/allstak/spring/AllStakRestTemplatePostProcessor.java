package dev.allstak.spring;

import dev.allstak.internal.SdkLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatically attaches the AllStak outbound HTTP interceptor to every
 * {@link RestTemplate} bean in the application context.
 *
 * <p>Spring Boot users have two common ways to obtain a {@code RestTemplate}:
 * <ol>
 *     <li>Injecting {@code RestTemplateBuilder} and calling {@code .build()} —
 *         handled by {@link AllStakRestTemplateCustomizer} because the builder
 *         auto-applies every {@code RestTemplateCustomizer} bean.</li>
 *     <li>Creating a {@code new RestTemplate()} directly in a {@code @Bean}
 *         method or with custom configuration — not covered by the customizer.
 *         This post-processor handles that path: it intercepts every
 *         {@code RestTemplate} bean after initialization and prepends the
 *         AllStak interceptor if not already present.</li>
 * </ol>
 *
 * <p>The check for {@code isAlreadyAttached(...)} guarantees we never attach
 * the same interceptor twice, even if the user's own customizer already
 * registered it (e.g. via both paths). This prevents duplicate outbound
 * events on a single HTTP call.
 */
public class AllStakRestTemplatePostProcessor implements BeanPostProcessor, Ordered {

    private final AllStakRestTemplateInterceptor interceptor;

    public AllStakRestTemplatePostProcessor(AllStakRestTemplateInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RestTemplate restTemplate) {
            if (!isAlreadyAttached(restTemplate)) {
                List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                interceptors.add(interceptor);
                restTemplate.setInterceptors(interceptors);
                SdkLogger.debug("AllStak attached outbound interceptor to RestTemplate bean '%s'", beanName);
            }
        }
        return bean;
    }

    private boolean isAlreadyAttached(RestTemplate restTemplate) {
        for (ClientHttpRequestInterceptor existing : restTemplate.getInterceptors()) {
            if (existing instanceof AllStakRestTemplateInterceptor) return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        // Run after the user's own BeanPostProcessors so the RestTemplate is
        // fully configured before we modify it.
        return Ordered.LOWEST_PRECEDENCE;
    }
}
