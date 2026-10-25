package com.ga.arqemio.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
//filter checks the HTTP requests before they reach controller.
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    //ConcurrentHashMap keeps the map thread safe when processed requests at the same time
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> requestTimes= new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //convert it to an HTTP request to access HTTP information
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //convert it into response to set code status
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //get the IP address of the client making request.
        String clientIp = httpRequest.getRemoteAddr();

        //get the current time in milliseconds
        long currentTime = System.currentTimeMillis();

        //checks whether the IP address already exists in requestCounts.
        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));
        //store the starting time for that IP address if it's not stored.
        requestTimes.putIfAbsent(clientIp, currentTime);
        //gets the request counter belonging to the current IP address.
        AtomicInteger counter = requestCounts.get(clientIp);

        int currentCount;
        //allows only one thread at a time to execute this block for the same counter object.
        synchronized (counter) {
            //checks whether 60 seconds have passed since the IP counting period
            if (currentTime - requestTimes.get(clientIp) >= 60_000) {
                //condition becomes true.
                //reset the request counter to zero
                counter.set(0);
                //update the starting time to the current time
                requestTimes.put(clientIp, currentTime);
            }
            //increase the request count by  and return the updated value
            currentCount = counter.incrementAndGet();
        }
        //check the request limit.
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            //if true return status and message
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        //allow tge request
        chain.doFilter(request, response);
    }


}