package cnadata.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CqrsConsumerStreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CqrsConsumerStreamsApplication.class, args);
    }

    /*
    @Bean
    public CommandLineRunner dataLoader(CustomLogDataRepository customLogDataRepository) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                customLogDataRepository.save(new CustomLogData("myywyi1", "{\"myywyi1\" :  \"logTest\", \"time\" : \"2021-10-08T11:30:46.401\"}"));
                customLogDataRepository.save(new CustomLogData("myywyi2", "{\"myywyi2\" :  \"logTest\", \"time\" : \"2021-10-08T11:30:46.401\"}"));
                customLogDataRepository.save(new CustomLogData("myywyi3", "{\"myywyi3\" :  \"logTest\", \"time\" : \"2021-10-08T11:30:46.401\"}"));
            }
        };
    }

     */
}
