package com.app.streamtest.domain.catalog

/** Gives the list of films and extra streams. Implemented in :data. */
interface CatalogRepository {
    suspend fun getCatalog(): Catalog
}
