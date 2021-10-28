package io.tjohander.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.net.URI
import java.time.Duration

@SpringBootApplication
class ClientApplication {

    companion object {
        private val log = LoggerFactory.getLogger(ClientApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            log.info("Starting client application...")
            runApplication<ClientApplication>(*args)
        }
    }

    @Bean
    fun webClient(builder: WebClient.Builder) = builder.build()

    @Bean
    fun ready(client: AvailabilityClient): ApplicationListener<ApplicationReadyEvent?>? {
        return ApplicationListener { applicationReadyEvent: ApplicationReadyEvent? ->
            arrayListOf<String>("ps5","xbox","ps4","switch").forEach { console ->
                Flux.range(0, 20)
                    .delayElements(Duration.ofMillis(100))
                    .log("Delayed element")
                    .subscribe { _ : Int? ->
                        client
                            .checkAvailability(console)
                            .log("Availability has been checked")
                            .subscribe { availability: Availability ->
                                log.info(
                                    "console: {}, availability: {} ",
                                    console,
                                    availability.isAvailable()
                                )
                            }
                    }
            }
        }
    }
}

data class Availability(
    private val available: Boolean,
    private val console: String
) {
    fun isAvailable() = this.available
}


@Component
class AvailabilityClient(
    @Autowired private val webClient: WebClient
) {
    fun checkAvailability(console: String) =
        this.webClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .scheme("http")
                    .host("localhost")
                    .port(8083)
                    .path("/availability/{console}")
                    .build(console)
            }
            .retrieve()
            .bodyToMono(Availability::class.java)
            .log("Result")
            .onErrorReturn(Availability(false, console))
}
