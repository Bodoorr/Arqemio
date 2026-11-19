package com.ga.company_service.config;

import com.ga.company_service.security.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            RateLimitingFilter rateLimitingFilter) {

        FilterRegistrationBean<RateLimitingFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(rateLimitingFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}