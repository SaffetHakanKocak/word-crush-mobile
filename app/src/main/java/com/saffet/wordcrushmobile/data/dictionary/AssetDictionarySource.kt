package com.saffet.wordcrushmobile.data.dictionary

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android `assets` klasöründen kelime listesi okuyan [DictionarySource].
 *
 * Varsayılan dosya yolu: `assets/dictionary/tr_words.txt`.
 *
 * Dosya formatı (assets/dictionary/tr_words.txt içinde belgelenmiştir):
 *  - Her satırda bir kelime.
 *  - `#` ile başlayan satırlar yorum sayılır ve atlanır.
 *  - Boş satırlar atlanır.
 *  - Baştaki/sondaki boşluklar temizlenir.
 *
 * IO işlemi [Dispatchers.IO] üzerinde yapılır; ana thread bloklanmaz.
 */
class AssetDictionarySource(
    context: Context,
    private val assetPath: String = DEFAULT_ASSET_PATH
) : DictionarySource {

    // Application context kullanıyoruz ki Activity context sızıntısı olmasın.
    private val appContext: Context = context.applicationContext

    override suspend fun load(): List<String> = withContext(Dispatchers.IO) {
        appContext.assets.open(assetPath).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith('#') }
                .toList()
        }
    }

    companion object {
        const val DEFAULT_ASSET_PATH: String = "dictionary/tr_words.txt"
    }
}
