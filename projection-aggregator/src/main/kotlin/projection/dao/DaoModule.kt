package projection.dao

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import common.model.event.eventBsonCodec
import io.ktor.server.config.ApplicationConfig
import org.bson.codecs.configuration.CodecRegistries
import org.koin.core.qualifier.named
import org.koin.dsl.module

val daoModule =
    module {
        single {
            val mongoConfig: ApplicationConfig by inject(named("mongo"))
            val connectionString = ConnectionString(mongoConfig.property("uri").getString())
            val codecRegistry =
                CodecRegistries.fromRegistries(
                    CodecRegistries.fromCodecs(eventBsonCodec),
                    MongoClientSettings.getDefaultCodecRegistry(),
                )
            val mongoClient =
                MongoClient.create(
                    MongoClientSettings
                        .builder()
                        .applyConnectionString(connectionString)
                        .codecRegistry(codecRegistry)
                        .build(),
                )
            mongoClient.getDatabase(mongoConfig.property("database").getString())
        }
        single { EventDao() }
        single { SnapshotDao() }
    }
