package com.thehecklers.playgroundkotlwfx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.util.*
import javax.annotation.PostConstruct
import kotlin.random.Random

@SpringBootApplication
class PlaygroundKotlWfxApplication

fun main(args: Array<String>) {
    runApplication<PlaygroundKotlWfxApplication>(*args)
}

@Component
class DataLoader(private val repo: ShipRepository) {

    @PostConstruct
    fun load() {
        val shipNames =
            listOf(
                "Ch'Tang",
                "Gr'oth",
                "Hegh'ta",
                "M'Char",
                "Maht-H'a",
                "Ning'tao",
                "Pagh",
                "T'Ong",
                "Vor'nak",
                "Ya'Vang"
            )
        val captains =
            listOf("Martok", "Koloth", "Kurn", "Kaybok", "Nu'Daq", "Lurkan", "Kargan", "K'Temoc", "Tanas")

        val rnd = Random

        (0..999).toFlux()
            .map {
                Ship(
                    name = shipNames.get(rnd.nextInt(shipNames.size)),
                    captain = captains.get(rnd.nextInt(captains.size))
                )
            }
            .flatMap { repo.save(it) }
            .subscribe { println(it) }

    }
}

@Configuration
class RouteConfig(private val repo: ShipRepository) {
    @Bean
    fun router() = router {
        listOf(
            GET("/ships") { req -> ok().body(repo.findAll()) },
            GET("/ships/{id}") { req -> ok().body(repo.findById(req.pathVariable("id"))) },
            GET("/search") { req -> ok().body(repo.findShipByCaptain(req.queryParam("captain"))) }
        )
    }
}

/*@RestController
@RequestMapping("/ships")
class ShipController(private val repo: ShipRepository) {
    @GetMapping
    fun getAllShips() = repo.findAll()

    @GetMapping("/{id}")
    fun getShipById(@PathVariable id: String) = repo.findById(id)

    @GetMapping("/search")
    fun getShipByCaptain(@RequestParam(defaultValue = "Martok") captain: String) = repo.findShipByCaptain(captain)
}*/

interface ShipRepository : ReactiveCrudRepository<Ship, String> {
    fun findShipByCaptain(captain: Optional<String>): Flux<Ship>
}

data class Ship(@Id val id: String? = null, val name: String, val captain: String)