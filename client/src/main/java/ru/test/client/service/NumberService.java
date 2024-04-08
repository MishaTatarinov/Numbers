package ru.test.client.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.test.client.observer.NumberCallback;
import ru.test.numbers.NumberRequest;
import ru.test.numbers.NumberServiceGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NumberService {
    private static final Logger log = LoggerFactory.getLogger(NumberService.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    @Value("${app.host}")
    private String host;
    @Value("${app.port}")
    private int port;


    public void getNumbers() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        NumberServiceGrpc.NumberServiceStub numberServiceStub =
                NumberServiceGrpc.newStub(channel);
        var latch = new CountDownLatch(1);
        NumberCallback numberCallback = new NumberCallback(latch);
        numberServiceStub.getNumbers(createNumberRequest(), numberCallback);

        Thread thread = getThread(numberCallback);

        executor.scheduleAtFixedRate(thread, 0, 1, TimeUnit.SECONDS);
        latch.await();
        channel.shutdownNow();
    }

    private Thread getThread(NumberCallback numberCallback) {
        AtomicInteger currentValue = new AtomicInteger();
        AtomicInteger lastValue = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();

        return new Thread(() -> {
            Integer responseValue = numberCallback.getValue();
            if (responseValue != null && responseValue != lastValue.get()) {
                lastValue.set(responseValue);
                currentValue.addAndGet(responseValue);
            }
            int value = currentValue.incrementAndGet();
            counter.incrementAndGet();
            log.info("current value:{}", value);
            if (counter.get() == 50) {
                executor.shutdown();
                log.info("The sequence is finished");
            }
        });
    }

    private NumberRequest createNumberRequest() {
        return NumberRequest.newBuilder()
                .setFirstValue(0)
                .setLastValue(30)
                .build();
    }
}
