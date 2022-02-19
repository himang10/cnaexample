/**
 * This example is a code explaining how to implement the Saga Architecture Pattern
 * using the CNA Data Platform. This example can be used by referring
 * to coaching or development using the CNA Data Platform.
 * (CNA: Cloud Native Application )
 *
 * @author Yong Woo Yi
 * @version 1.0
 * @since 2022
 */

package cnadata.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SagaParticipantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaParticipantApplication.class, args);
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
