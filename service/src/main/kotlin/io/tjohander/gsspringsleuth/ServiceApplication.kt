package io.tjohander.gsspringsleuth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@SpringBootApplication
class ServiceApplication {

    companion object {
        private val log = LoggerFactory.getLogger(ServiceApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            log.info("Starting client application...")
            runApplication<ServiceApplication>(*args)
        }
    }
}

@RestController
class AvailabilityController {

    companion object {
        private val log = LoggerFactory.getLogger(ServiceApplication::class.java)
    }

    @GetMapping("/availability/{console}")
    fun getAvailability(@PathVariable console: String): Map<String, Any> {
        log.info("Handling availability request for {} ", console)
        return mapOf(
            Pair("console", console),
            Pair("available", checkAvailability(console))
        )
    }

    private fun validate(console: String): Boolean =
        StringUtils.hasText(console) && setOf("ps5", "ps4", "switch", "xbox").contains(console)

    private fun checkAvailability(console: String): Boolean {
        log.info("Checking availability for {}", console)
        Assert.state(validate(console), "the console specified, $console, is not valid")
        return when (console) {
            "ps5" -> throw RuntimeException("Service Exception")
            "xbox" -> true
            else -> false
        }
    }
}
