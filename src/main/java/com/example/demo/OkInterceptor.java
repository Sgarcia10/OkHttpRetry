package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Fallback;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

@Slf4j
public class OkInterceptor implements Interceptor {
    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>()
                .handleResult(null)
                .handleResultIf(result -> result.code() == 503)
                .withDelay(Duration.ofSeconds(1))
                .withMaxRetries(3)
                .onFailedAttempt(e -> log.error("Connection attempt failed", e.getLastFailure()))
                .onRetry(e -> {
                    e.getLastResult().close();
                    log.warn("Failure #{}. Retrying.", e.getAttemptCount());
                });
        Request request = chain.request();

        long t1 = System.nanoTime();
        log.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = Failsafe
                .with(retryPolicy)
                .get(() -> getResponse(chain, request));

        long t2 = System.nanoTime();
        log.info(String.format("Received response for %s in %.1fms%n%s",
                response.code(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }

    @NotNull
    private Response getResponse(Chain chain, Request request) throws IOException {
        Response r = chain.proceed(request);
        return r;
    }
}
