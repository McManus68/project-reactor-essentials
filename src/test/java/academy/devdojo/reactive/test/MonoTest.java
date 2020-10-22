package academy.devdojo.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/*
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. Publisher sends all the objects requested.
 * 2. Publisher send all the objects it has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
 */
public class MonoTest {

    @Test
    public void monoSubscriber(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();

        log.info("------------------");
        StepVerifier.create(mono)
                .expectNext("William Davids")
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));

        log.info("------------------");
        StepVerifier.create(mono)
                .expectNext("William Davids")
                .verifyComplete();

    }

    @Test
    public void monoSubscriberError(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing Mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);


        log.info("------------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

    }

    @Test
    public void monoSubscriberConsumerComplete(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"));

        log.info("------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void monoSubscriberConsumerSubscription(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription ->  subscription.request(5l));

        log.info("------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

    }

    @Test
    public void monoDoOnMethods(){
        String name = "William Davids";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(value -> log.info("Request Received, starting doing something.."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnNext(s -> log.info("Value is here 2. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed"));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription ->  subscription.request(5l));

        log.info("------------------");
    }

    @Test
    public void monoDoOnError(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Nude"))
                .doOnError(e -> log.error("Error message {}", e.getMessage()))
                .doOnNext(s -> log.info("Not Displayed"))
                .log();


        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume(){
        String name = "William Davids";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Nude"))
                .doOnError(e -> log.error("Error message {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside onErrorResume");
                    return Mono.just(name);
                })
                .log();


        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn(){
        Mono<Object> error = Mono.error(new IllegalArgumentException("Nude"))
                .doOnError(e -> log.error("Error message {}", e.getMessage()))
                .onErrorReturn("EMPTY")
                .log();


        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
