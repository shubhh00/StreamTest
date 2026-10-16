package com.app.streamtest.data.catalog

import android.content.Context
import com.app.streamtest.domain.catalog.Catalog
import com.app.streamtest.domain.catalog.CatalogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads the catalog from assets/streams.json, which is bundled inside the APK. */
@Singleton
class AssetCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : CatalogRepository {

    // The file can't change while the app runs, so read it once and keep the result.
    private var cached: Catalog? = null

    override suspend fun getCatalog(): Catalog {
        cached?.let { return it }
        val text = withContext(Dispatchers.IO) {
            context.assets.open("streams.json").bufferedReader().use { it.readText() }
        }
        return parseCatalog(text).also { cached = it }
    }
}
