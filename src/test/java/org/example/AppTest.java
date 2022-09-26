package org.example;


import jdk.jfr.StackTrace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

   private List<String> rawStrings;
   private Flux<String> publisher;

   @BeforeEach
   void init(){
       rawStrings = Arrays.asList("Pack", "my","box","with","five","dozen", "liquor", "jugs");
       publisher =  Flux.fromIterable(rawStrings);
   }

    @Test
    public void shouldAnswerWithTrue()
    {
        rawStrings.forEach(System.out::println);
        Flux.fromIterable(rawStrings).subscribe(System.out::println);
    }

    @Test
    void simpleSubscriber(){
        publisher.log().subscribe(System.out::println);
    }

    @Test
    void simpleSubscribeToEvents(){
        publisher.subscribe(
                n -> System.out.println(n),
                error -> System.out.println("received an error::" + error),
                () -> System.out.println(" *** received on complete event ***")
        );
    }

    @Test
    void moreThanOneSubscriber(){
       Flux<Integer>  intPub = Flux.range(1,20);

       intPub.subscribe(e -> System.out.printf("first sub%s%n", e));
        intPub.subscribe(e -> System.out.printf("second sub%s%n", e));
    }

    @Test
    void publishStreamOfValues(){
        Flux uuid  = Flux.fromStream(Stream.generate(UUID::randomUUID).limit (20));
        uuid.log().subscribe(e -> System.out.printf("subs:: %s%n", e));

    }

    //simultaneous broadcast
    @Test
    void multipleSubscribers(){
        ConnectableFlux concurrentPub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit (5)).publish();

        concurrentPub.log().subscribe(e -> System.out.printf("first:: %s%n", e));
        concurrentPub.log().subscribe(e -> System.out.printf("second:: %s%n", e));

        concurrentPub.connect();
    }
}
