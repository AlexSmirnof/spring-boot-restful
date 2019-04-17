package org.jetbrains.kotlin.demo.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.router
import reactor.util.function.Tuple2


operator fun <T1,T2> Tuple2<T1,T2>.component1() = t1
operator fun <T1,T2> Tuple2<T1,T2>.component2() = t2

@Configuration
class Routes(private val handlers: ContactHandlers) {

    @Bean
    fun contactRoute() = router {
        "/api/contacts".nest {
            GET("/", handlers::all )
            GET("/{id}", handlers::one)
            POST("/", handlers::save)
            DELETE("/",handlers::remove)
            PUT("/{id}/mobiles",handlers::addMobile)
            PUT("/{id}/addresses",handlers::addAddress)
        }
    }
}

@Component
class ContactHandlers(private val repository: ContactRepository) {

    fun one(request: ServerRequest) = request.pathVariable("id").let {
        ok().body(repository.findById(it))
    }

    fun all(request: ServerRequest) = ok().body(repository.findAll())

    fun save(request: ServerRequest) = request.bodyToMono<Contact>()
            .flatMap { ok().body(repository.save(it)) }

    fun remove(request: ServerRequest) = request.pathVariable("id").let {
        ok().body(repository.deleteById(it))
    }

    fun addMobile(request: ServerRequest) = request.pathVariable("id").let {
        repository.findById(it)
                .zipWith(request.bodyToMono<String>())
                .map { (contact, mobile) -> contact.copy(mobiles = listOf(*contact.mobiles.toTypedArray(), mobile)) }
                .flatMap { ok().body(repository.save(it)) }
    }

    fun addAddress(request: ServerRequest) = request.pathVariable("id").let {
        repository.findById(it)
                .zipWith(request.bodyToMono<String>())
                .map { (contact, address) -> contact.copy(addresses = listOf(*contact.addresses.toTypedArray(), address)) }
                .flatMap { ok().body(repository.save(it)) }
    }
}

interface ContactRepository: ReactiveMongoRepository<Contact,String>

@Document
data class Contact(
        @Id val id: String,
        val lastName: String,
        val firstName: String,
        val fatherName: String,
        val address: String,
        val addresses: List<String>,
        val mobile: String,
        val mobiles: List<String>,
        val work: String,
        val birthDate: String
)





