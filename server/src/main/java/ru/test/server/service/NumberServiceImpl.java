package ru.test.server.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.test.numbers.NumberRequest;
import ru.test.numbers.NumberResponse;
import ru.test.numbers.NumberServiceGrpc;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class NumberServiceImpl extends NumberServiceGrpc.NumberServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(NumberServiceImpl.class);
    private ScheduledExecutorService executor;

    @Override
    public void getNumbers(NumberRequest request,
                       StreamObserver<NumberResponse> responseObserver) {
        log.info("Request for a new sequence of numbers from {} to {}",
                request.getFirstValue(), request.getLastValue());
        executor = Executors.newSingleThreadScheduledExecutor();

        Thread thread = getThread(request, responseObserver);

        executor.scheduleAtFixedRate(thread, 0, 2, TimeUnit.SECONDS);
    }

    private Thread getThread(NumberRequest request, StreamObserver<NumberResponse> responseObserver) {
        AtomicInteger currentValue = new AtomicInteger(request.getFirstValue());

        return new Thread(() -> {
            int value = currentValue.incrementAndGet();
            NumberResponse response = NumberResponse.newBuilder()
                    .setGenerateValue(value)
                    .build();
            responseObserver.onNext(response);
            if (value == request.getLastValue()) {
                executor.shutdown();
                responseObserver.onCompleted();
                log.info("The sequence is finished");
            }
        });
    }
}
