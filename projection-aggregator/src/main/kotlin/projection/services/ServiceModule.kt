package projection.services

import org.koin.dsl.module

val serviceModule =
    module {
        single { AggregationService() }
    }
