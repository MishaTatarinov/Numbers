package ru.test.client.observer;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.test.numbers.NumberResponse;

import java.util.concurrent.CountDownLatch;

public class NumberCallback implements StreamObserver<NumberResponse> {

    private static final Logger log = LoggerFactory.getLogger(NumberCallback.class);
    private Integer value;
    private final CountDownLatch latch;

    public NumberCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public void onNext(NumberResponse response) {
        log.info("new value:{}", response.getGenerateValue());
        this.value = response.getGenerateValue();
    }

    @Override
    public void onError(Throwable t) {
        log.info("Received error: ", t);
        latch.countDown();

    }

    @Override
    public void onCompleted() {
        log.info("Request completed");
        latch.countDown();
    }
}
