package com.thehecklers.playgroundkotlwfx

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.time.Duration
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

/*@Configuration
class CoRouterConfig(private val repo: ShipRepository) {
    @Bean
    fun crouter() = coRouter {
        GET("/ships", ::getAllShips)
        GET("/ships/{id}", ::getShipById)
        GET("/search", ::getShipByCaptain)
    }

    suspend fun getAllShips(req: ServerRequest) = ok().body(repo.findAll()).awaitSingle()

    suspend fun getShipById(req: ServerRequest) = ok()
        .body(repo.findById(req.pathVariable("id"))).awaitSingle()

    suspend fun getShipByCaptain(req: ServerRequest) = ok()
        .body(repo.findShipByCaptain(req.queryParam("captain"))).awaitSingle()
}*/

/*
@RestController
@RequestMapping("/ships")
class ShipController(private val repo: ShipRepository) {
    @GetMapping
    suspend fun getAllShips() = repo.findAll().asFlow()

    @GetMapping("/{id}")
    suspend fun getShipById(@PathVariable id: String) = repo.findById(id).asFlow()

    @GetMapping("/search")
    suspend fun getShipByCaptain(@RequestParam(defaultValue = "Martok") captain: String) = repo.findShipByCaptain(captain).asFlow()
}
*/

@Configuration
class RouteConfig(private val repo: ShipRepository) {
    @Bean
    fun router() = router {
        GET("/ships") { req -> ok().body(repo.findAll()) }
        GET("/ships/{id}") { req -> ok().body(repo.findById(req.pathVariable("id"))) }
        GET("/search") { req -> ok().body(repo.findShipByCaptain(req.queryParam("captain"))) }
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
//    fun findShipByCaptain(captain: String): Flux<Ship>
    fun findShipByCaptain(captain: Optional<String>): Flux<Ship>
}

data class Ship(@Id val id: String? = null, val name: String, val captain: String)