package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.GameConfig
import com.saffet.wordcrushmobile.domain.model.SpecialType
import kotlin.math.abs
import kotlin.random.Random

/**
 * Word Crush oyununun UI'dan bağımsız çekirdek motoru.
 *
 * Bu sınıf yalnızca Kotlin stdlib'ine bağımlıdır; hiçbir Android veya
 * Compose sembolü kullanılmaz. Bu sayede kolayca JVM üzerinde unit test
 * edilebilir.
 *
 * Sorumluluklar:
 *  - Yeni grid üretmek (Türkçe harf frekansına göre ağırlıklı).
 *  - Kare grid olarak yalnızca 6x6, 8x8, 10x10 boyutlarını desteklemek.
 *  - 8 yönlü komşuluk kontrolü yapmak.
 *  - Kullanıcının harf seçimini doğrulamak
 *    (tekrar seçim engeli + komşuluk + minimum uzunluk).
 *  - Hücre zincirinden kelime metni üretmek.
 *  - Başarısızlık durumunda nedeni [SelectionResult.Rejected] ile raporlamak.
 *
 * [random] enjekte edilebilir — testlerde sabit seed vererek deterministik
 * davranış elde etmek için kullanılır.
 */
class WordCrushEngine(
    private val random: Random = Random.Default,
    private val powerUpResolver: PowerUpResolver = PowerUpResolver()
) {

    // --- Grid üretimi ---------------------------------------------------

    /**
     * Şartnamede tanımlı kare boyutlardan biriyle grid oluşturur.
     *
     * @throws IllegalArgumentException [size] desteklenen kümenin dışındaysa.
     */
    fun generateBoard(size: Int): List<List<Cell>> {
        require(size in SUPPORTED_SIZES) {
            "Desteklenen boyutlar: $SUPPORTED_SIZES, verilen: $size"
        }
        return generateBoard(rows = size, cols = size)
    }

    /** [GameConfig] ile grid oluşturur. Config'in rows/cols değerleri kullanılır. */
    fun generateBoard(config: GameConfig): List<List<Cell>> =
        generateBoard(rows = config.rows, cols = config.cols)

    /**
     * Belirli satır ve sütun sayılarıyla grid oluşturur.
     * Her hücreye ağırlıklı rastgele seçilmiş bir Türkçe harf atanır.
     */
    fun generateBoard(rows: Int, cols: Int): List<List<Cell>> {
        require(rows > 0 && cols > 0) {
            "Grid boyutu pozitif olmalı: rows=$rows, cols=$cols"
        }
        return List(rows) { row ->
            List(cols) { col ->
                Cell(
                    row = row,
                    col = col,
                    letter = TurkishLetterPool.randomLetter(random)
                )
            }
        }
    }

    /**
     * Tahtadaki hücre içeriklerini (harf + özel tip) koruyup konumlarını
     * rastgele yeniden dağıtır.
     *
     * Bu işlem harf frekans dağılımını bozmaz; yalnızca yerleşimi değiştirir.
     * Dead-board kurtarma adımlarında (0 kelime) kontrollü ilk müdahale olarak
     * kullanılabilir.
     */
    fun reshuffleBoard(board: List<List<Cell>>): List<List<Cell>> {
        if (board.isEmpty()) return board
        val rows = board.size
        val cols = board.first().size
        require(board.all { it.size == cols }) {
            "Tahta satırlarının sütun sayıları eşit olmalı (dikdörtgen grid)."
        }

        val payload = board.flatten().map {
            Cell(
                row = 0,
                col = 0,
                letter = it.letter,
                special = it.special,
                isSelected = false
            )
        }.toMutableList()
        payload.shuffle(random)

        var i = 0
        return List(rows) { r ->
            List(cols) { c ->
                val cell = payload[i++]
                cell.copy(row = r, col = c, isSelected = false)
            }
        }
    }

    // --- Üst seviye seçim API'si ----------------------------------------

    /**
     * Mevcut seçime yeni bir hücre eklemeyi dener.
     *
     * Başarılıysa [SelectionResult.Accepted] döner ve güncel seçim
     * (yeni hücre eklenmiş hâliyle) ile o ana kadar oluşan kelime metni
     * paylaşılır. Başarısızsa [SelectionResult.Rejected] + uygun
     * [SelectionError] döner.
     *
     * Reddedilme sebepleri öncelik sırasıyla:
     *  1. [SelectionError.CELL_ALREADY_SELECTED]
     *  2. [SelectionError.NOT_NEIGHBOR]
     */
    fun appendCell(current: List<Cell>, candidate: Cell): SelectionResult {
        if (containsCell(current, candidate)) {
            return SelectionResult.Rejected(SelectionError.CELL_ALREADY_SELECTED)
        }
        val last = current.lastOrNull()
        if (last != null && !areNeighbors(last, candidate)) {
            return SelectionResult.Rejected(SelectionError.NOT_NEIGHBOR)
        }
        val newSelection = current + candidate
        return SelectionResult.Accepted(
            selection = newSelection,
            word = buildWord(newSelection)
        )
    }

    /**
     * Tamamlanmış bir seçimin (kullanıcı parmağını bıraktığında) son
     * doğrulamasını yapar. Bu kontrol, sözlük kontrolünden önce
     * yapısal geçerliliği sağlar.
     *
     * Reddedilme sebepleri öncelik sırasıyla:
     *  1. [SelectionError.EMPTY_SELECTION]
     *  2. [SelectionError.BROKEN_CHAIN]        (aynı hücre tekrar veya zincir kopuk)
     *  3. [SelectionError.TOO_SHORT]
     *
     * Not: Sözlük kontrolü motorun sorumluluğunda değildir; ayrı bir
     * bileşen (örn. WordValidator) tarafından yapılmalıdır.
     */
    fun validateSelection(
        selection: List<Cell>,
        minLength: Int = MIN_WORD_LENGTH
    ): SelectionResult {
        if (selection.isEmpty()) {
            return SelectionResult.Rejected(SelectionError.EMPTY_SELECTION)
        }
        if (!isValidChain(selection)) {
            return SelectionResult.Rejected(SelectionError.BROKEN_CHAIN)
        }
        if (selection.size < minLength) {
            return SelectionResult.Rejected(SelectionError.TOO_SHORT)
        }
        return SelectionResult.Accepted(
            selection = selection,
            word = buildWord(selection)
        )
    }

    /**
     * Hücre listesinin harflerini sırayla birleştirerek kelime metnini döndürür.
     */
    fun buildWord(selection: List<Cell>): String =
        selection.joinToString(separator = "") { it.letter.toString() }

    // --- Düşük seviye kontroller ----------------------------------------

    /**
     * İki hücrenin 8 yönlü komşu olup olmadığını döndürür.
     *
     * Aynı hücre komşu sayılmaz; kendisi ile kelime oluşturulamaz.
     * Yönler: yukarı/aşağı/sağ/sol + dört çapraz.
     */
    fun areNeighbors(a: Cell, b: Cell): Boolean {
        if (isSameCell(a, b)) return false
        val dr = abs(a.row - b.row)
        val dc = abs(a.col - b.col)
        return dr <= 1 && dc <= 1
    }

    /**
     * Mevcut [selection] zincirine [candidate] hücresinin boolean olarak
     * eklenebilir olup olmadığını döndürür.
     *
     * UI'da hücre önizlemesi (highlight) için hızlı boolean cevap gerektiğinde
     * kullanılır. Sebep bilgisi isteniyorsa [appendCell] tercih edilmelidir.
     */
    fun canAppend(selection: List<Cell>, candidate: Cell): Boolean {
        if (containsCell(selection, candidate)) return false
        val last = selection.lastOrNull() ?: return true
        return areNeighbors(last, candidate)
    }

    /**
     * Bir hücre listesinin baştan sona geçerli bir seçim zinciri olup
     * olmadığını kontrol eder. Her ardışık çift komşu olmalı ve aynı hücre
     * tekrar kullanılmamalıdır. Boş liste "geçerli" sayılır (henüz başlamamış).
     */
    fun isValidChain(selection: List<Cell>): Boolean {
        val seen = HashSet<Long>(selection.size)
        for (i in selection.indices) {
            val cell = selection[i]
            if (!seen.add(cellKey(cell))) return false
            if (i > 0 && !areNeighbors(selection[i - 1], cell)) return false
        }
        return true
    }

    /**
     * Seçim, geçerli bir kelime oluşturabilecek minimum uzunlukta mı?
     * Şartnameye göre varsayılan minimum 3 harftir.
     */
    fun hasMinLength(selection: List<Cell>, minLength: Int = MIN_WORD_LENGTH): Boolean =
        selection.size >= minLength

    // --- Grid güncelleme (gravity + refill) -----------------------------

    /**
     * Geçerli kelime oluşturulduktan sonra tahtayı günceller.
     *
     * Adımlar (şartnamedeki "Harf Patlatma Mekaniği" ile uyumlu):
     *  1. [clearedCells] konumlarındaki harfler tahtadan silinir.
     *  2. Silinen hücrelerin üstünde kalan harfler, yerçekimi mantığıyla
     *     aşağı doğru düşer (sütun bazında).
     *  3. Üstte oluşan boşluklar [TurkishLetterPool] üzerinden ağırlıklı
     *     rastgele yeni harflerle doldurulur.
     *  4. Grid boyutu değişmez — her zaman dikdörtgen formu korunur.
     *
     * Saf fonksiyon: Girdi tahtasını değiştirmez; yeni bir matris döndürür.
     * Deterministik test için [random] constructor üzerinden enjekte edilir.
     *
     * @param board         Güncel tahta.
     * @param clearedCells  Kelime oluştururken kullanılan ve artık silinecek
     *                      hücreler (seçim zinciri). Sırası önemli değildir;
     *                      yalnızca (row, col) pozisyonları dikkate alınır.
     * @return Yeni, tamamen dolu tahta.
     */
    fun collapseAndRefill(
        board: List<List<Cell>>,
        clearedCells: List<Cell>,
        preserve: Map<Long, Cell> = emptyMap()
    ): List<List<Cell>> {
        if (board.isEmpty()) return board
        val rows = board.size
        val cols = board.first().size

        val clearedKeys: Set<Long> = clearedCells.mapTo(HashSet(clearedCells.size)) { cellKey(it) }
        if (clearedKeys.isEmpty() && preserve.isEmpty()) return board

        val newColumns: Array<Array<Cell?>> = Array(cols) { arrayOfNulls<Cell>(rows) }

        for (c in 0 until cols) {
            // Sütunu "sabit" (preserve) konumlarla segmentlere böl. Her segment
            // bağımsız collapse yapar — PDF §6: özel simgeli son harf YERİNDE
            // KALIR; yalnızca onun üstündeki yaşayanlar kendi segmentlerinde
            // aşağı düşer, eksikler üstten yeni harflerle dolar.
            var segmentStart = 0
            for (r in 0..rows) {
                val isEnd = r == rows
                val fixedHere: Cell? =
                    if (!isEnd) preserve[positionKey(r, c)] else null
                if (fixedHere != null || isEnd) {
                    fillSegment(
                        board = board,
                        col = c,
                        fromInclusive = segmentStart,
                        toExclusive = r,
                        clearedKeys = clearedKeys,
                        preserve = preserve,
                        out = newColumns[c]
                    )
                    if (fixedHere != null) {
                        newColumns[c][r] = fixedHere.copy(
                            row = r,
                            col = c,
                            isSelected = false
                        )
                        segmentStart = r + 1
                    }
                }
            }
        }

        return List(rows) { r ->
            List(cols) { c -> newColumns[c][r]!! }
        }
    }

    /**
     * Bir sütunun [fromInclusive, toExclusive) aralığını yerçekimi mantığıyla
     * doldurur: temizlenenler çıkarılır, yaşayanlar alta yığılır, üst boşluklar
     * yeni rastgele harflerle doldurulur. Preserve edilmiş pozisyonlar bu
     * aralıkta olamaz (çağıran taraf zaten segment sınırı olarak kullanır).
     */
    private fun fillSegment(
        board: List<List<Cell>>,
        col: Int,
        fromInclusive: Int,
        toExclusive: Int,
        clearedKeys: Set<Long>,
        preserve: Map<Long, Cell>,
        out: Array<Cell?>
    ) {
        val size = toExclusive - fromInclusive
        if (size <= 0) return

        val survivors = ArrayList<Cell>(size)
        for (r in fromInclusive until toExclusive) {
            val key = positionKey(r, col)
            // Preserve edilmiş hücreler zaten segmenti BİTİRİR, yani bu
            // aralıkta görülmezler; güvenlik için yine de atla.
            if (preserve.containsKey(key)) continue
            if (!clearedKeys.contains(key)) {
                survivors.add(board[r][col])
            }
        }

        val emptyCount = size - survivors.size
        for (i in 0 until size) {
            val absoluteRow = fromInclusive + i
            out[absoluteRow] = if (i < emptyCount) {
                Cell(
                    row = absoluteRow,
                    col = col,
                    letter = TurkishLetterPool.randomLetter(random)
                )
            } else {
                val surv = survivors[i - emptyCount]
                surv.copy(row = absoluteRow, col = col, isSelected = false)
            }
        }
    }

    // --- Yüksek seviye: kelime + özel güç uygulama ----------------------

    /**
     * Geçerli bir kelime (seçim) için PDF §6 "Harf Patlatma Mekaniği"ni
     * uygulayan atomik, üst-seviye API.
     *
     * Akış:
     *  1. Seçimdeki HER hücrenin mevcut özel gücü (varsa) aktive edilir;
     *     [PowerUpResolver] etkilenen ek koordinatları döner.
     *  2. Temizlenecek pozisyonlar = (seçim ∪ ekstra patlamalar).
     *  3. Kelime uzunluğuna göre [PowerUpRule] yeni bir özel tip belirler.
     *     Özel tip NONE değilse **son hücre korunur**: konumu cleared'dan
     *     çıkarılır ve yeni tipiyle aynı yere oturtulur (PDF: "son harf
     *     yerinde kalır ve özel simgeye dönüşür").
     *  4. [collapseAndRefill] segmentli modda çağrılır; korunan hücre
     *     etrafındaki yaşayanlar kendi segmentinde aşağı düşer, eksikler
     *     üstten yeni harflerle dolar.
     *
     * Skor hesabı motorun sorumluluğu değildir; [ApplyWordResult]
     * çağırana yeterli bilgi verir (tüm patlayan hücrelerin kümesi).
     */
    fun applyWord(
        board: List<List<Cell>>,
        selection: List<Cell>
    ): ApplyWordResult {
        if (board.isEmpty() || selection.isEmpty()) {
            return ApplyWordResult(
                newBoard = board,
                removedPositions = emptySet(),
                triggeredSpecials = emptyList(),
                plantedSpecial = null
            )
        }

        // Selection hücreleri UI snapshot'ı olabileceği için özel tip bilgisini
        // tahtanın canlı hâlinden oku.
        val triggered = ArrayList<SpecialType>()
        val extra = HashSet<BoardPosition>()
        for (sel in selection) {
            val live = board[sel.row][sel.col]
            if (live.special != SpecialType.NONE) {
                triggered.add(live.special)
                extra.addAll(powerUpResolver.affected(board, live))
            }
        }

        val last = selection.last()
        val newType = PowerUpRule.forWordLength(selection.size)
        val planted: SpecialPlacement? =
            if (newType != SpecialType.NONE) {
                SpecialPlacement(last.row, last.col, newType)
            } else null

        // Temizlenecek tüm konumların birleşimi.
        val removed = HashSet<BoardPosition>(selection.size + extra.size)
        for (c in selection) removed.add(BoardPosition(c.row, c.col))
        removed.addAll(extra)

        // Eğer özel bırakılacaksa son hücreyi koru.
        val preserve = HashMap<Long, Cell>()
        if (planted != null) {
            val lastPos = BoardPosition(last.row, last.col)
            removed.remove(lastPos)
            val liveLast = board[last.row][last.col]
            preserve[positionKey(last.row, last.col)] = liveLast.copy(
                special = newType,
                isSelected = false
            )
        }

        val clearedList = removed.map { board[it.row][it.col] }
        val newBoard = collapseAndRefill(board, clearedList, preserve)

        return ApplyWordResult(
            newBoard = newBoard,
            removedPositions = removed,
            triggeredSpecials = triggered,
            plantedSpecial = planted
        )
    }

    // --- Yardımcılar ----------------------------------------------------

    /** İki hücre aynı pozisyonda mı? (letter farkı önemsiz) */
    private fun isSameCell(a: Cell, b: Cell): Boolean =
        a.row == b.row && a.col == b.col

    private fun containsCell(selection: List<Cell>, cell: Cell): Boolean =
        selection.any { isSameCell(it, cell) }

    /**
     * (row, col) çiftini tek bir Long anahtara paketler. HashSet için
     * Cell'in varsayılan equals'ına güvenmek yerine pozisyon bazlı eşitlik
     * kullanıyoruz (letter, isSelected farkı eşitliği bozmasın diye).
     */
    private fun cellKey(cell: Cell): Long = positionKey(cell.row, cell.col)

    private fun positionKey(row: Int, col: Int): Long =
        (row.toLong() shl 32) or (col.toLong() and 0xFFFFFFFFL)

    companion object {
        /** Şartnamede zorunlu olan minimum kelime uzunluğu. */
        const val MIN_WORD_LENGTH: Int = 3

        /** Şartnamede tanımlı desteklenen kare grid boyutları. */
        val SUPPORTED_SIZES: Set<Int> = setOf(6, 8, 10)
    }
}
