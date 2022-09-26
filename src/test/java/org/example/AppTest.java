package org.example;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private List<String> rawStrings;
    private Flux<String> publisher;

    @BeforeEach
    void init() {
        rawStrings = Arrays.asList("Pack", "my", "box", "with", "five", "dozen", "liquor", "jugs");
        publisher = Flux.fromIterable(rawStrings);
    }

    @Test
    public void shouldAnswerWithTrue() {
        rawStrings.forEach(System.out::println);
        Flux.fromIterable(rawStrings).subscribe(System.out::println);
    }

    @Test
    void simpleSubscriber() {
        publisher.log().subscribe(System.out::println);
    }

    @Test
    void simpleSubscribeToEvents() {
        publisher.subscribe(
                System.out::println,
                error -> System.out.println("received an error::" + error),
                () -> System.out.println(" *** received on complete event ***")
        );
    }

    @Test
    void moreThanOneSubscriber() {
        Flux<Integer> intPub = Flux.range(1, 20);

        intPub.subscribe(e -> System.out.printf("first sub%s%n", e));
        intPub.subscribe(e -> System.out.printf("second sub%s%n", e));
    }

    @Test
    void publishStreamOfValues() {
        Flux<UUID> uuid = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(20));
        uuid.log().subscribe(e -> System.out.printf("subs:: %s%n", e));
    }

    //simultaneous broadcast
    @Test
    void multipleSubscribers() {
        ConnectableFlux<UUID> concurrentPub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(5)).publish();

        concurrentPub.log().subscribe(e -> System.out.printf("first:: %s%n", e));
        concurrentPub.log().subscribe(e -> System.out.printf("second:: %s%n", e));

        concurrentPub.connect();
    }

    @Test
    void multipleSubscribersWithHandlers() {
        ConnectableFlux<UUID> concurrentPub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(5)).publish();

        concurrentPub.log().subscribe(e -> System.out.printf("first:: %s%n", e), error -> System.out.println("received an error::" + error));
        concurrentPub.log().subscribe(e -> System.out.printf("second:: %s%n", e));

        concurrentPub.connect();
    }

    @Test
    void multipleSubscribersWithErrorHandlers() {
        ConnectableFlux<Integer> concurrentPub = Flux.fromStream(Stream.generate(() -> 5 / 0).limit(5)).publish();
        concurrentPub.log().subscribe(e -> System.out.printf("first:: %s%n", e), error -> System.out.println("received an error::" + error), () -> System.out.println(" *** received on complete event ***"));
        concurrentPub.connect();
    }

    @Test
    void subscribeToPublisherWithErrorPublished() {
        Flux<Integer> number = Flux.range(1, 20).log().map(e -> {
            if (e == 8) {
                throw new RuntimeException("error on the 8th");
            }
            return e;
        });
        number.subscribe(e -> System.out.printf("Value received %s%n", e),
                error -> System.err.println("Error Published:: " + error),
                () -> System.out.println("received on complete event"));
    }


    @Test
    void disposablePublisher(){
        Flux<UUID> idStream = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(10)).log().delayElements(Duration.ofSeconds(3));
        Disposable subscriberRef = idStream.subscribe(u -> System.out.printf("subscriber receive %s%n", u),
                error-> System.err.println("Error published:: " +error),
                () -> System.out.println("Complete event published"));

        Runnable runnableTask = ()  -> {
          try {
              TimeUnit.SECONDS.sleep(12);
          }catch(InterruptedException ie){
              ie.printStackTrace();
          }
          System.out.println("Cancelling subscription");
          subscriberRef.dispose();
        };

        runnableTask.run();
    }

    @Test
    void reactScheduler(){
        Scheduler reactScheduler = Schedulers.newParallel("pub-parallel", 2);
        Flux<String> phrasePublish = Flux.range(1,20).map(i -> 42 +i).publishOn(reactScheduler).map(m -> {
            var v = Thread.currentThread().getName();
            return String.format("%s value produced:: %s",v,m);
        });

        Runnable r0 = () -> phrasePublish.subscribe(n -> System.out.printf("sub received:: %s%n",n));

        r0.run();
    }
}
