package ru.test.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.test.client.service.NumberService;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext applicationContext = SpringApplication.run(ClientApplication.class, args);
        NumberService numberService = applicationContext.getBean(NumberService.class);

        numberService.getNumbers();
    }
}
