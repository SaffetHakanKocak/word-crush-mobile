package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saffet.wordcrushmobile.WordCrushApp
import com.saffet.wordcrushmobile.data.repository.GameHistoryRepository
import com.saffet.wordcrushmobile.data.repository.JokerInventoryRepository
import com.saffet.wordcrushmobile.domain.combo.ComboAnalyzer
import com.saffet.wordcrushmobile.domain.combo.ComboResult
import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import com.saffet.wordcrushmobile.domain.engine.ApplyWordResult
import com.saffet.wordcrushmobile.domain.engine.AvailableWordCounter
import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.engine.SelectionError
import com.saffet.wordcrushmobile.domain.engine.SelectionResult
import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine
import com.saffet.wordcrushmobile.domain.joker.JokerAction
import com.saffet.wordcrushmobile.domain.joker.JokerEngine
import com.saffet.wordcrushmobile.domain.joker.JokerResult
import com.saffet.wordcrushmobile.domain.joker.JokerTargetSpec
import com.saffet.wordcrushmobile.domain.model.SpecialType
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.GameConfig
import com.saffet.wordcrushmobile.domain.model.GameRecord
import com.saffet.wordcrushmobile.domain.model.JokerType
import com.saffet.wordcrushmobile.domain.model.PlayedWord
import com.saffet.wordcrushmobile.domain.score.ScoreModifiers
import com.saffet.wordcrushmobile.domain.score.WordScore
import com.saffet.wordcrushmobile.domain.score.WordScoreCalculator
import com.saffet.wordcrushmobile.domain.usecase.EnsurePlayableBoardUseCase
import com.saffet.wordcrushmobile.domain.usecase.Intervention
import com.saffet.wordcrushmobile.domain.usecase.ValidateWordUseCase
import com.saffet.wordcrushmobile.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GameScreen'in state'ini ve iş kurallarını yöneten ViewModel.
 *
 * Mimari:
 *  - Navigation args (`rows`, `cols`, `moves`) [SavedStateHandle] üzerinden
 *    okunur ve bir [GameConfig] haline getirilir.
 *  - [WordCrushEngine] saf Kotlin motor; seçim doğrulamasını yapar.
 *  - [ValidateWordUseCase] sözlük kontrolünü asenkron yapar.
 *  - UI, yalnızca [uiState] Flow'unu gözlemler; geri yönde sadece
 *    [onCellTapped], [onSubmitWord], [onClearSelection], [onDismissMessage]
 *    ve [onRestart] çağrılarını tetikler.
 *
 * State değişikliklerinin tamamı tek bir [MutableStateFlow.update] noktası
 * üzerinden akar; böylece UI tutarsız ara hâlleri göremez.
 */
class GameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val engine: WordCrushEngine,
    private val dictionaryRepository: DictionaryRepository,
    private val validateWord: ValidateWordUseCase,
    private val scoreCalculator: WordScoreCalculator,
    private val comboAnalyzer: ComboAnalyzer,
    private val historyRepository: GameHistoryRepository,
    private val jokerInventoryRepository: JokerInventoryRepository,
    private val jokerEngine: JokerEngine = JokerEngine(),
    private val availableWordCounter: AvailableWordCounter = AvailableWordCounter(),
    private val ensurePlayableBoard: EnsurePlayableBoardUseCase
) : AndroidViewModel(application) {

    /** Navigation'dan gelen oyun konfigürasyonu. */
    private val config: GameConfig = readConfig(savedStateHandle)

    /** Oyunun başlama anı (epoch millis). Süre hesabı için referans. */
    private var startedAtMs: Long = System.currentTimeMillis()

    /**
     * Mevcut oturumun DB'ye kaydedilip kaydedilmediğini izler. Aynı oyunun
     * iki kere yazılmasını engeller (örn. hamle bittikten sonra kullanıcı
     * geri bastığında).
     */
    private var hasPersisted: Boolean = false

    /**
     * Aktif "tahtadaki oluşturulabilir kelime sayısı" hesabı. Tahta her
     * değiştiğinde eskisi iptal edilip yenisi başlatılır; böylece geride
     * kalan hesap güncel olmayan bir sonuç yazmaz.
     */
    private var availableWordsJob: Job? = null
    private var gravityAnimationId: Long = 0L

    private val _uiState = MutableStateFlow(
        GameUiState(
            board = engine.generateBoard(config),
            remainingMoves = config.totalMoves
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Sözlük yükleme durumunu takip et — UI "Onayla" butonunu buna göre
        // enable/disable edebilir. Application.onCreate zaten preload'u
        // tetiklediği için bu çağrı genellikle hemen hazır döner.
        viewModelScope.launch {
            dictionaryRepository.preload()
            _uiState.update { it.copy(isDictionaryReady = true) }
            // Başlangıçta çözülemez bir grid görünmemesi için ilk tahta
            // analiz + gerekirse müdahale adımını kullanıcıya göstermeden yap.
            recomputeAvailableWordsAndRecoverIfNeeded(showInterventionMessage = false)
        }

        // Joker envanteri akışını state'e bağla. Market'ten satın alma
        // ya da oyunda kullanım olduğunda UI sayaçları kendiliğinden
        // güncellenir.
        viewModelScope.launch {
            jokerInventoryRepository.observeAll().collect { inv ->
                _uiState.update { it.copy(jokerInventory = inv) }
            }
        }
    }

    // --- UI olayları ---------------------------------------------------

    /**
     * Bir hücreye tıklandığında çağrılır. Davranış:
     *  - Seçim zincirinin son hücresine tıklandıysa → geri al (pop).
     *  - Yeni bir hücre ise → [WordCrushEngine.appendCell] çağrılır.
     *  - Motor reddederse sebep [GameUiState.lastMessage]'a yazılır.
     */
    fun onCellTapped(cell: Cell) {
        if (_uiState.value.isGameOver) return

        // Joker hedef seçme modundaysa tıklamalar seçim zincirine değil,
        // jokerin hedef listesine gider.
        if (_uiState.value.jokerTargeting != null) {
            onJokerTargetPicked(BoardPosition(cell.row, cell.col))
            return
        }

        val current = _uiState.value.selectedCells
        val last = current.lastOrNull()

        if (last != null && last.row == cell.row && last.col == cell.col) {
            val shortened = current.dropLast(1)
            _uiState.update {
                it.copy(
                    selectedCells = shortened,
                    currentWord = engine.buildWord(shortened),
                    lastMessage = null
                )
            }
            return
        }

        when (val result = engine.appendCell(current, cell)) {
            is SelectionResult.Accepted -> _uiState.update {
                it.copy(
                    selectedCells = result.selection,
                    currentWord = result.word,
                    lastMessage = null
                )
            }
            is SelectionResult.Rejected -> _uiState.update {
                it.copy(lastMessage = messageFor(result.reason))
            }
        }
    }

    /**
     * Mevcut seçimi kelime olarak onaylamayı dener.
     *
     * Hamle muhasebesi (PDF §Oyun Akışı):
     *  - Yapısal doğrulama ([WordCrushEngine.validateSelection]) geçemezse
     *    ("kelime oluşturulamadı" durumu) hamle HARCANMAZ.
     *  - Yapısal olarak geçerli bir kelime oluşturulduysa — sözlükte olmasa
     *    bile — **bir hamle harcanır.**
     *    • Sözlükte varsa: skor eklenir, tahta collapse+refill'e uğrar.
     *    • Sözlükte yoksa: skor ve tahta değişmez; yalnızca hamle düşer.
     */
    fun onSubmitWord() {
        if (_uiState.value.isGameOver) return

        val selection = _uiState.value.selectedCells
        when (val structural = engine.validateSelection(selection)) {
            is SelectionResult.Rejected -> _uiState.update {
                it.copy(lastMessage = messageFor(structural.reason))
            }
            is SelectionResult.Accepted -> viewModelScope.launch {
                val accepted = validateWord(structural.word)
                if (accepted) {
                    // PDF §Combo: ana kelime sözlükte varsa ayrıca içindeki
                    // tüm 3+ harfli, geçerli alt kelimeleri de tespit et.
                    val combo = comboAnalyzer.analyze(structural.word)
                    acceptWord(combo, structural.selection)
                } else {
                    rejectWordAndConsumeMove(structural.word)
                }
            }
        }
    }

    // --- Drag gesture API ----------------------------------------------

    /**
     * PDF §Oyun Akışı: "harfler üzerinde parmağını sürükleyerek seçer".
     *
     * Parmağın ilk temas ettiği hücre seçim zincirinin başlangıcıdır.
     * Mevcut seçim varsa tamamen sıfırlanır; böylece yarım kalmış bir
     * seçim üstüne yeni bir drag başlayabilir. Joker targeting modunda
     * veya oyun bittiğinde sessizce ignore edilir — UI tarafı zaten
     * drag layer'ını [enableDrag]=false ile devre dışı bırakır, ama bu
     * savunmacı kontrol yarış durumlarına karşı korur.
     */
    fun onDragStartCell(row: Int, col: Int) {
        val state = _uiState.value
        if (state.isGameOver || state.jokerTargeting != null) return
        if (row !in state.board.indices || col !in state.board[row].indices) return

        val first = state.board[row][col]
        _uiState.update {
            it.copy(
                selectedCells = listOf(first),
                currentWord = engine.buildWord(listOf(first)),
                lastMessage = null
            )
        }
    }

    /**
     * Drag sırasında parmak yeni bir hücreye girdiğinde çağrılır.
     *
     * Üç durum:
     *  - Hedef seçim zincirinin son hücresiyse → hiçbir şey yapma
     *    (aynı hücre içinde mikro hareketler yok sayılır).
     *  - Hedef bir önceki hücreyse → son hücreyi zincirden çıkar
     *    ("geri sürükleme" ile pop). Hamle harcanmaz.
     *  - Başka bir geçerli komşu hücre ise → [WordCrushEngine.appendCell]
     *    ile eklenir. Motor reddederse (örn. komşu değil, zaten seçili)
     *    drag sessizce devam eder; mesajla kullanıcı kesilmez.
     */
    fun onDragOverCell(row: Int, col: Int) {
        val state = _uiState.value
        if (state.isGameOver || state.jokerTargeting != null) return
        if (row !in state.board.indices || col !in state.board[row].indices) return

        val current = state.selectedCells
        if (current.isEmpty()) {
            onDragStartCell(row, col)
            return
        }

        val last = current.last()
        if (last.row == row && last.col == col) return

        // Pop: parmak bir önceki hücreye dönerse zincirden son halkayı sil.
        if (current.size >= 2) {
            val prev = current[current.size - 2]
            if (prev.row == row && prev.col == col) {
                val shortened = current.dropLast(1)
                _uiState.update {
                    it.copy(
                        selectedCells = shortened,
                        currentWord = engine.buildWord(shortened),
                        lastMessage = null
                    )
                }
                return
            }
        }

        val cell = state.board[row][col]
        when (val res = engine.appendCell(current, cell)) {
            is SelectionResult.Accepted -> _uiState.update {
                it.copy(
                    selectedCells = res.selection,
                    currentWord = res.word,
                    lastMessage = null
                )
            }
            // Sessizce ignore — drag devam ediyor, kullanıcıya engel olma.
            is SelectionResult.Rejected -> Unit
        }
    }

    /**
     * Kullanıcı parmağını grid üzerinde kaldırdığında çağrılır.
     *
     * Selection boş değilse mevcut [onSubmitWord] akışına devredilir →
     * yapısal doğrulama + sözlük kontrolü + hamle muhasebesi aynen çalışır.
     * Böylece hamle kuralları drag ile click arasında tutarlı kalır.
     */
    fun onDragEnd() {
        val state = _uiState.value
        if (state.isGameOver || state.jokerTargeting != null) return
        if (state.selectedCells.isEmpty()) return
        onSubmitWord()
    }

    /**
     * Drag grid dışında biterse veya sistem tarafından iptal edilirse
     * (çok parmaklı jest, telefon kilitleme vb.) seçimi sessizce temizler.
     * Hamle harcanmaz — kullanıcı "vazgeçmiş" sayılır.
     */
    fun onDragCancel() {
        if (_uiState.value.selectedCells.isEmpty()) return
        _uiState.update {
            it.copy(
                selectedCells = emptyList(),
                currentWord = "",
                lastMessage = null
            )
        }
    }

    /** Seçimi sıfırlar; kelime ve mesaj alanlarını temizler. */
    fun onClearSelection() {
        _uiState.update {
            it.copy(
                selectedCells = emptyList(),
                currentWord = "",
                lastMessage = null
            )
        }
    }

    /** UI geçici mesajı dismiss ettiğinde (ör. Snackbar kapandığında). */
    fun onDismissMessage() {
        _uiState.update { it.copy(lastMessage = null) }
    }

    /**
     * Yeni oyun tahtası üretir ve tüm ilerlemeyi sıfırlar.
     * Mevcut oturum henüz kaydedilmediyse "abandoned" olarak kaydedilir.
     */
    fun onRestart() {
        persistIfNeeded(abandoned = true)
        startedAtMs = System.currentTimeMillis()
        hasPersisted = false
        _uiState.update {
            GameUiState(
                board = engine.generateBoard(config),
                remainingMoves = config.totalMoves,
                isDictionaryReady = it.isDictionaryReady,
                isBoardReady = false
            )
        }
        recomputeAvailableWordsAndRecoverIfNeeded(showInterventionMessage = false)
    }

    /**
     * Kullanıcı oyun ekranından çıkmak üzereyken (geri tuşu / navigate up)
     * UI katmanı tarafından çağrılır. Oyun doğal olarak bittiyse (isGameOver)
     * tekrar kayıt yapmaz; aksi halde abandoned=true ile kaydeder.
     */
    fun onExitGame() {
        persistIfNeeded(abandoned = !_uiState.value.isGameOver)
    }

    // --- Joker API ------------------------------------------------------

    /**
     * UI alt çubuğundan bir joker butonuna basıldığında çağrılır.
     *
     * Davranış:
     *  - Envanter 0 ise sessizce ignore edilir ve hata mesajı verilir.
     *  - 0 hedefli jokerler (FISH, LETTER_SHUFFLE, PARTY_BOOSTER) HEMEN
     *    uygulanır; envanter -1 düşer.
     *  - Hedefli jokerler için [GameUiState.jokerTargeting] set edilir;
     *    sonraki hücre tıklamaları hedef olarak alınır.
     */
    fun onJokerPressed(type: JokerType) {
        if (_uiState.value.isGameOver) return
        if (_uiState.value.jokerTargeting?.type == type) {
            // Aynı jokere tekrar basıldı → modu iptal et (toggle davranışı).
            onJokerCancel()
            return
        }
        val owned = _uiState.value.jokerInventory[type] ?: 0
        if (owned <= 0) {
            _uiState.update { it.copy(lastMessage = "${type.displayName} envanterde yok") }
            return
        }

        val spec = JokerTargetSpec.of(type)
        if (spec.neededTargets == 0) {
            // Hedefsiz joker → doğrudan çalıştır.
            executeJoker(buildZeroTargetAction(type))
            return
        }

        // Hedefli joker → targeting moduna gir. Eğer daha önce bir
        // kelime seçimi yapılmışsa onu temizleyerek kullanıcıya net başla.
        _uiState.update {
            it.copy(
                selectedCells = emptyList(),
                currentWord = "",
                lastMessage = targetPrompt(type, spec.neededTargets),
                jokerTargeting = JokerTargetingState(
                    type = type,
                    neededTargets = spec.neededTargets,
                    pickedTargets = emptyList(),
                    requiresAdjacent = spec.requiresAdjacentTargets
                )
            )
        }
    }

    /**
     * Kullanıcı targeting modunu manuel iptal ettiğinde (ör. "Vazgeç"
     * butonu) çağrılır. Envantere dokunulmaz.
     */
    fun onJokerCancel() {
        _uiState.update {
            it.copy(
                jokerTargeting = null,
                lastMessage = "Joker iptal edildi"
            )
        }
    }

    /**
     * Targeting modunda bir hücreye tıklandığında çağrılır (internal dispatcher).
     * Hedef toplama tamamlandığında jokeri çalıştırır.
     */
    private fun onJokerTargetPicked(pos: BoardPosition) {
        val state = _uiState.value.jokerTargeting ?: return

        // FREE_SWAP: ikinci hedef ilkine komşu olmalı.
        if (state.requiresAdjacent && state.pickedTargets.size == 1) {
            val first = state.pickedTargets[0]
            if (first == pos) {
                _uiState.update { it.copy(lastMessage = "Aynı hücre iki kez seçilemez") }
                return
            }
            if (!arePositionsAdjacent(first, pos)) {
                _uiState.update { it.copy(lastMessage = "Hedefler komşu olmalı") }
                return
            }
        }

        val updated = state.copy(pickedTargets = state.pickedTargets + pos)
        if (!updated.isComplete) {
            _uiState.update { it.copy(jokerTargeting = updated) }
            return
        }

        // Hedef sayısı tamamlandı — aksiyonu derle ve çalıştır.
        val action = buildTargetedAction(updated) ?: run {
            onJokerCancel()
            return
        }
        executeJoker(action)
    }

    private fun buildZeroTargetAction(type: JokerType): JokerAction = when (type) {
        JokerType.FISH           -> JokerAction.Fish()
        JokerType.LETTER_SHUFFLE -> JokerAction.LetterShuffle
        JokerType.PARTY_BOOSTER  -> JokerAction.PartyBooster
        else -> error("Hedefsiz joker değil: $type")
    }

    private fun buildTargetedAction(state: JokerTargetingState): JokerAction? =
        when (state.type) {
            JokerType.WHEEL           -> JokerAction.Wheel(state.pickedTargets[0])
            JokerType.LOLLIPOP_HAMMER -> JokerAction.Lollipop(state.pickedTargets[0])
            JokerType.FREE_SWAP       -> JokerAction.FreeSwap(
                a = state.pickedTargets[0],
                b = state.pickedTargets[1]
            )
            else -> null
        }

    /**
     * Jokeri tahta üzerinde çalıştırır, başarılıysa envanteri -1 düşer
     * ve state'i günceller. Hamle SAYISI harcanmaz — joker "yardım"dır.
     *
     * Akış iki fazlıdır, böylece "yok etme" görsel olarak algılanabilir:
     *  1. Silinecek pozisyonlar [GameUiState.explodingPositions] alanına
     *     yazılır; board hâlâ eskidir. UI cell'leri kısa bir flash+scale
     *     animasyonuyla vurgular.
     *  2. [EXPLOSION_ANIM_MS] sonra yeni board (gravity+refill uygulanmış)
     *     set edilir, explodingPositions temizlenir.
     */
    private fun executeJoker(action: JokerAction) {
        val currentBoard = _uiState.value.board
        when (val res = jokerEngine.apply(currentBoard, action)) {
            is JokerResult.Success -> {
                viewModelScope.launch {
                    // Faz 1: flash — yalnızca removedPositions dolu olan
                    // jokerler için anlamlı (FISH, WHEEL, LOLLIPOP).
                    // SHUFFLE / PARTY_BOOSTER'da board tamamen değişir,
                    // removedPositions boş — flash adımı atlanır, yalnızca
                    // board swap edilir; harf değişim animasyonu (Cell
                    // içindeki AnimatedContent) geçişi yine belirgin yapar.
                    val effect = buildJokerEffectState(
                        action = action,
                        result = res,
                        board = currentBoard
                    )
                    if (effect != null || res.removedPositions.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                explodingPositions = res.removedPositions,
                                jokerEffect = effect
                            )
                        }
                        delay(jokerEffectDuration(action, res))
                    }
                    val gravityAnimation = buildGravityAnimationState(
                        oldBoard = currentBoard,
                        removedPositions = res.removedPositions,
                        preservedPositions = emptySet()
                    )
                    // Faz 2: yeni board'u yerleştir, flash'ı temizle.
                    _uiState.update {
                        it.copy(
                            board = res.newBoard,
                            selectedCells = emptyList(),
                            currentWord = "",
                            jokerTargeting = null,
                            explodingPositions = emptySet(),
                            jokerEffect = null,
                            specialEffects = emptyList(),
                            gravityAnimation = gravityAnimation,
                            lastMessage = "${action.type.displayName} kullanıldı"
                        )
                    }
                    jokerInventoryRepository.adjust(action.type, delta = -1)
                    recomputeAvailableWordsAndRecoverIfNeeded()
                }
            }
            is JokerResult.InvalidTarget -> _uiState.update {
                it.copy(
                    jokerTargeting = null,
                    lastMessage = invalidTargetMessage(res.reason)
                )
            }
        }
    }

    private fun buildJokerEffectState(
        action: JokerAction,
        result: JokerResult.Success,
        board: List<List<Cell>>
    ): JokerEffectState? {
        result.swapped?.let { swapped ->
            return JokerEffectState(
                type = action.type,
                affectedPositions = setOf(swapped.first, swapped.second),
                swapped = swapped
            )
        }

        if (action.type == JokerType.LETTER_SHUFFLE) {
            return JokerEffectState(
                type = action.type,
                affectedPositions = allBoardPositions(board),
                wholeBoard = true
            )
        }

        if (result.removedPositions.isNotEmpty()) {
            return JokerEffectState(
                type = action.type,
                affectedPositions = result.removedPositions,
                wholeBoard = action.type == JokerType.PARTY_BOOSTER
            )
        }

        return null
    }

    private fun allBoardPositions(board: List<List<Cell>>): Set<BoardPosition> =
        buildSet {
            for (row in board.indices) {
                for (col in board[row].indices) {
                    add(BoardPosition(row, col))
                }
            }
        }

    private fun jokerEffectDuration(
        action: JokerAction,
        result: JokerResult.Success
    ): Long =
        when {
            result.swapped != null -> JOKER_SWAP_ANIM_MS
            action.type == JokerType.LETTER_SHUFFLE -> JOKER_SHUFFLE_ANIM_MS
            else -> EXPLOSION_ANIM_MS
        }

    private fun invalidTargetMessage(reason: JokerResult.InvalidTarget.Reason): String =
        when (reason) {
            JokerResult.InvalidTarget.Reason.OUT_OF_BOUNDS        -> "Geçersiz hedef"
            JokerResult.InvalidTarget.Reason.TARGETS_NOT_ADJACENT -> "Hedefler komşu olmalı"
            JokerResult.InvalidTarget.Reason.TARGETS_SAME_CELL    -> "Aynı hücre iki kez seçilemez"
            JokerResult.InvalidTarget.Reason.BOARD_EMPTY          -> "Tahta boş"
        }

    private fun targetPrompt(type: JokerType, needed: Int): String =
        if (needed == 1) "${type.displayName}: hedef hücreyi seç"
        else "${type.displayName}: komşu 2 hücre seç"

    private fun arePositionsAdjacent(a: BoardPosition, b: BoardPosition): Boolean {
        val dr = kotlin.math.abs(a.row - b.row)
        val dc = kotlin.math.abs(a.col - b.col)
        return (dr + dc) > 0 && dr <= 1 && dc <= 1
    }

    // --- Yardımcılar ---------------------------------------------------

    /**
     * Combo sonucunu ve seçilen hücreleri alarak ana kelimeyi kabul eder.
     *
     * Puan hesabı (PDF §Combo): ana kelimenin taban skoru + her geçerli alt
     * kelimenin taban skoru tek bir toplamda birleştirilir. foundWords'e
     * yalnızca ana kelime tek bir [PlayedWord] olarak eklenir — böylece
     * "bulunan kelime sayısı" metriği hamle başına 1 artar (istatistikler
     * için ana kelime yeterli).
     *
     * Akış iki fazlıdır (bkz. [executeJoker]):
     *  1. Seçilen hücreler [GameUiState.explodingPositions] olarak
     *     işaretlenir; board hâlâ eskidir → UI flash+scale animasyonu yapar.
     *  2. [EXPLOSION_ANIM_MS] sonra gravity+refill uygulanmış yeni board
     *     set edilir, skor / hamle sayacı güncellenir.
     */
    private suspend fun acceptWord(combo: ComboResult, cells: List<Cell>) {
        val mainScore: WordScore = scoreCalculator.calculate(
            word = combo.mainWord,
            modifiers = ScoreModifiers.NONE
        )
        val subScoreTotal: Int = combo.subWords.sumOf { sub ->
            scoreCalculator.calculate(sub, ScoreModifiers.NONE).total
        }
        val totalPoints: Int = mainScore.total + subScoreTotal

        val played = PlayedWord(
            word = combo.mainWord,
            score = totalPoints,
            cells = cells
        )

        // PDF §6 "Harf Patlatma Mekaniği": seçimdeki özeller tetiklenir,
        // kelime uzunluğuna göre son hücreye yeni bir özel simge bırakılır.
        // applyWord bu adımı atomik yapar — çıktısı yeni board + meta.
        val boardBefore = _uiState.value.board
        val result = engine.applyWord(boardBefore, cells)

        // Faz 1: seçili hücreleri patlama vurgusu ile göster; board hâlâ
        // eski. `selectedCells`'i boşaltıyoruz ki seçim vurgusu flash'ın
        // önüne geçmesin.
        val explodingPositions = result.removedPositions
        val specialEffects = buildSpecialEffectStates(boardBefore, cells)
        val preservedPositions = result.plantedSpecial
            ?.let { setOf(BoardPosition(it.row, it.col)) }
            ?: emptySet()
        val gravityAnimation = buildGravityAnimationState(
            oldBoard = boardBefore,
            removedPositions = result.removedPositions,
            preservedPositions = preservedPositions
        )
        _uiState.update { s ->
            s.copy(
                selectedCells = emptyList(),
                currentWord = "",
                explodingPositions = explodingPositions,
                specialEffects = specialEffects,
                gravityAnimation = null,
                lastMessage = buildAcceptMessage(combo, totalPoints, result)
            )
        }
        delay(EXPLOSION_ANIM_MS)

        // Faz 2: gravity+refill sonucu yeni board'u yerleştir.
        _uiState.update { s ->
            val newMoves = s.remainingMoves - 1
            s.copy(
                board = result.newBoard,
                score = s.score + totalPoints,
                remainingMoves = newMoves,
                foundWords = s.foundWords + played,
                isGameOver = newMoves <= 0,
                explodingPositions = emptySet(),
                specialEffects = emptyList(),
                gravityAnimation = gravityAnimation
            )
        }

        // Tahta değişti — yeni board için kelime sayımını yeniden başlat.
        recomputeAvailableWordsAndRecoverIfNeeded()

        // Hamleler bittiyse oyun doğal olarak tamamlandı → kaydet.
        if (_uiState.value.isGameOver) {
            persistIfNeeded(abandoned = false)
        }
    }

    /**
     * Snackbar mesajını combo durumuna göre biçimlendirir.
     *
     * Örnekler:
     *  - Combo yok    → `+7 · "SARI"`
     *  - 2× combo     → `+10 · "SARI" · 2× combo (ARI)`
     *  - 4× combo     → `+N · "ADANA" · 4× combo (DANA, ANA, ADA)`
     */
    private fun comboMessage(combo: ComboResult, totalPoints: Int): String {
        if (!combo.isCombo) {
            return "+$totalPoints · \"${combo.mainWord}\""
        }
        val subList = combo.subWords.joinToString(", ")
        return "+$totalPoints · \"${combo.mainWord}\" · ${combo.comboCount}× combo ($subList)"
    }

    /**
     * Kombu mesajına PDF §6 "Harf Patlatma Mekaniği" bilgilerini ekler:
     *  - Hamle sırasında aktive olan özel güç(ler) (varsa)
     *  - Bu hamlede bırakılan yeni özel güç (varsa)
     */
    private fun buildAcceptMessage(
        combo: ComboResult,
        totalPoints: Int,
        result: ApplyWordResult
    ): String {
        val base = comboMessage(combo, totalPoints)
        val fragments = mutableListOf<String>()
        if (result.triggeredSpecials.isNotEmpty()) {
            val names = result.triggeredSpecials.joinToString(", ") {
                specialTypeLabel(it)
            }
            fragments.add("$names aktif")
        }
        result.plantedSpecial?.let {
            fragments.add("${specialTypeLabel(it.type)} bırakıldı")
        }
        return if (fragments.isEmpty()) base
        else "$base · ${fragments.joinToString(" · ")}"
    }

    private fun buildGravityAnimationState(
        oldBoard: List<List<Cell>>,
        removedPositions: Set<BoardPosition>,
        preservedPositions: Set<BoardPosition>
    ): GravityAnimationState? {
        if (oldBoard.isEmpty()) return null
        if (removedPositions.isEmpty() && preservedPositions.isEmpty()) return null

        val rows = oldBoard.size
        val cols = oldBoard.firstOrNull()?.size ?: return null
        val motions = LinkedHashMap<BoardPosition, GravityCellMotion>()

        for (col in 0 until cols) {
            var segmentStart = 0
            for (row in 0..rows) {
                val atEnd = row == rows
                val fixedHere = !atEnd && BoardPosition(row, col) in preservedPositions
                if (fixedHere || atEnd) {
                    appendGravitySegmentMotions(
                        col = col,
                        fromInclusive = segmentStart,
                        toExclusive = row,
                        removedPositions = removedPositions,
                        preservedPositions = preservedPositions,
                        motions = motions
                    )
                    if (fixedHere) segmentStart = row + 1
                }
            }
        }

        if (motions.isEmpty()) return null
        gravityAnimationId += 1
        return GravityAnimationState(id = gravityAnimationId, motions = motions)
    }

    private fun appendGravitySegmentMotions(
        col: Int,
        fromInclusive: Int,
        toExclusive: Int,
        removedPositions: Set<BoardPosition>,
        preservedPositions: Set<BoardPosition>,
        motions: MutableMap<BoardPosition, GravityCellMotion>
    ) {
        val size = toExclusive - fromInclusive
        if (size <= 0) return

        val survivorRows = ArrayList<Int>(size)
        for (row in fromInclusive until toExclusive) {
            val pos = BoardPosition(row, col)
            if (pos !in removedPositions && pos !in preservedPositions) {
                survivorRows.add(row)
            }
        }

        val emptyCount = size - survivorRows.size
        for (i in 0 until emptyCount) {
            val targetRow = fromInclusive + i
            val entryRows = emptyCount - i
            motions[BoardPosition(targetRow, col)] = GravityCellMotion(
                initialOffsetRows = -entryRows.toFloat(),
                distanceRows = entryRows,
                isNewLetter = true
            )
        }

        survivorRows.forEachIndexed { index, oldRow ->
            val targetRow = fromInclusive + emptyCount + index
            val offsetRows = oldRow - targetRow
            if (offsetRows != 0) {
                motions[BoardPosition(targetRow, col)] = GravityCellMotion(
                    initialOffsetRows = offsetRows.toFloat(),
                    distanceRows = kotlin.math.abs(offsetRows),
                    isNewLetter = false
                )
            }
        }
    }

    private fun buildSpecialEffectStates(
        board: List<List<Cell>>,
        cells: List<Cell>
    ): List<SpecialEffectState> =
        cells.mapNotNull { selected ->
            val live = board.getOrNull(selected.row)?.getOrNull(selected.col) ?: return@mapNotNull null
            if (live.special == SpecialType.NONE) return@mapNotNull null
            val origin = BoardPosition(live.row, live.col)

            SpecialEffectState(
                type = live.special,
                origin = origin,
                affectedPositions = affectedPositionsForSpecial(
                    type = live.special,
                    origin = origin,
                    rows = board.size,
                    cols = board.firstOrNull()?.size ?: 0
                )
            )
        }

    private fun affectedPositionsForSpecial(
        type: SpecialType,
        origin: BoardPosition,
        rows: Int,
        cols: Int
    ): Set<BoardPosition> =
        buildSet {
            when (type) {
                SpecialType.NONE -> Unit
                SpecialType.ROW_CLEAR -> {
                    for (col in 0 until cols) add(BoardPosition(origin.row, col))
                }
                SpecialType.COLUMN_CLEAR -> {
                    for (row in 0 until rows) add(BoardPosition(row, origin.col))
                }
                SpecialType.AREA_BLAST,
                SpecialType.MEGA_BLAST -> {
                    val radius = if (type == SpecialType.AREA_BLAST) 1 else 2
                    for (row in (origin.row - radius)..(origin.row + radius)) {
                        for (col in (origin.col - radius)..(origin.col + radius)) {
                            if (row in 0 until rows && col in 0 until cols) {
                                add(BoardPosition(row, col))
                            }
                        }
                    }
                }
            }
        }

    private fun specialTypeLabel(type: SpecialType): String = when (type) {
        SpecialType.NONE         -> ""
        SpecialType.ROW_CLEAR    -> "Satır Temizleme"
        SpecialType.AREA_BLAST   -> "Alan Patlatma"
        SpecialType.COLUMN_CLEAR -> "Sütun Temizleme"
        SpecialType.MEGA_BLAST   -> "Mega Patlatma"
    }

    /**
     * Yapısal olarak geçerli ama sözlükte bulunmayan kelime için çağrılır.
     *
     * PDF §Oyun Akışı: "hatalı kelime olsa bir hamle harcanmış olur."
     * Bu nedenle skor ve tahta sabit kalır; yalnızca hamle sayacı düşer ve
     * seçim temizlenir. Hamleler biterse [persistIfNeeded] ile kayıt atılır.
     */
    private fun rejectWordAndConsumeMove(word: String) {
        _uiState.update { s ->
            val newMoves = s.remainingMoves - 1
            s.copy(
                selectedCells = emptyList(),
                currentWord = "",
                remainingMoves = newMoves,
                isGameOver = newMoves <= 0,
                lastMessage = "\"$word\" kelime sözlükte yok"
            )
        }
        recomputeAvailableWordsAndRecoverIfNeeded()
        if (_uiState.value.isGameOver) {
            persistIfNeeded(abandoned = false)
        }
    }

    /**
     * Mevcut oyunu [GameHistoryRepository] üzerinden Room'a yazar.
     * [hasPersisted] bayrağıyla idempotenttir: aynı oturum iki defa yazılmaz.
     */
    private fun persistIfNeeded(abandoned: Boolean) {
        if (hasPersisted) return
        val snapshot = _uiState.value
        // Hiç hamle yapılmadan çıkıldıysa anlamlı bir kayıt olmaz, atla.
        if (snapshot.foundWords.isEmpty() && snapshot.score == 0 &&
            snapshot.remainingMoves == config.totalMoves
        ) {
            return
        }
        hasPersisted = true

        val record = GameRecord(
            id = 0,
            playedAt = startedAtMs,
            rows = config.rows,
            cols = config.cols,
            totalMoves = config.totalMoves,
            movesUsed = config.totalMoves - snapshot.remainingMoves,
            score = snapshot.score,
            wordCount = snapshot.foundWords.size,
            longestWord = snapshot.foundWords.maxByOrNull { it.word.length }?.word.orEmpty(),
            durationSeconds = ((System.currentTimeMillis() - startedAtMs) / 1000L)
                .coerceAtLeast(0L),
            abandoned = abandoned
        )
        viewModelScope.launch { historyRepository.save(record) }
    }

    override fun onCleared() {
        // Sistem ViewModel'i temizlerken (ör. process death) kullanıcı
        // oyundan ayrılıyor sayılır; açıkta kalan bir oturumu kaydet.
        persistIfNeeded(abandoned = !_uiState.value.isGameOver)
        super.onCleared()
    }

    /**
     * Mevcut tahtadaki **benzersiz geçerli kelime sayısını** arka planda
     * hesaplar ve UI state'ine yazar.
     *
     * - Hesaplama [Dispatchers.Default] üzerinde çalışır: DFS + sözlük
     *   taraması CPU-bound iştir, UI thread'ini kilitlememesi kritiktir.
     * - Önceki bir hesap hâlâ çalışıyorsa iptal edilir — yalnızca güncel
     *   tahtanın sonucu state'e yazılsın.
     * - Sözlük henüz hazır değilse (`isReady = false`) hiç başlatmaz;
     *   preload tamamlandığında [init] bloğu tekrar tetikler.
     */
    private fun recomputeAvailableWordsAndRecoverIfNeeded(
        showInterventionMessage: Boolean = true
    ) {
        availableWordsJob?.cancel()
        if (!dictionaryRepository.isReady()) return

        val boardSnapshot = _uiState.value.board
        availableWordsJob = viewModelScope.launch {
            val dict = dictionaryRepository.snapshot()
            val recovery = withContext(Dispatchers.Default) {
                ensurePlayableBoard.ensure(
                    board = boardSnapshot,
                    config = config,
                    dictionary = dict
                )
            }

            // Bu hesap çalışırken tahta değiştiyse sonuç stale'dir, yazma.
            if (_uiState.value.board != boardSnapshot) return@launch

            val interventionMessage =
                if (showInterventionMessage) messageFor(recovery.intervention) else null

            _uiState.update { state ->
                state.copy(
                    board = recovery.board,
                    availableWordCount = recovery.availableWordCount,
                    selectedCells = if (recovery.board != boardSnapshot) emptyList() else state.selectedCells,
                    currentWord = if (recovery.board != boardSnapshot) "" else state.currentWord,
                    explodingPositions = if (recovery.board != boardSnapshot) emptySet() else state.explodingPositions,
                    jokerEffect = if (recovery.board != boardSnapshot) null else state.jokerEffect,
                    isBoardReady = true,
                    lastMessage = interventionMessage ?: state.lastMessage
                )
            }
        }
    }

    private fun messageFor(intervention: Intervention): String? = when (intervention) {
        Intervention.NONE -> null
        Intervention.RESHUFFLED -> "Tahta otomatik karıştırıldı"
        Intervention.REGENERATED -> "Tahta otomatik yenilendi"
        Intervention.GUARANTEED_WORD_INJECTED -> "Tahta kurtarıldı: en az 1 kelime garanti"
        Intervention.FAILED -> "Tahta şu an kurtarılamadı, tekrar deneniyor"
    }

    private fun messageFor(reason: SelectionError): String = when (reason) {
        SelectionError.CELL_ALREADY_SELECTED -> "Bu harf zaten seçili"
        SelectionError.NOT_NEIGHBOR          -> "Yalnızca komşu harfleri seçebilirsin"
        SelectionError.TOO_SHORT             -> "Kelime en az 3 harf olmalı"
        SelectionError.EMPTY_SELECTION       -> "Önce harf seçmelisin"
        SelectionError.BROKEN_CHAIN          -> "Seçim tutarsız, lütfen yeniden dene"
    }

    private fun readConfig(handle: SavedStateHandle): GameConfig {
        val rows: Int = handle[Screen.Game.ARG_ROWS] ?: DEFAULT_SIZE
        val cols: Int = handle[Screen.Game.ARG_COLS] ?: DEFAULT_SIZE
        val moves: Int = handle[Screen.Game.ARG_MOVES] ?: DEFAULT_MOVES
        return GameConfig(rows = rows, cols = cols, totalMoves = moves)
    }

    companion object {
        private const val DEFAULT_SIZE = 6
        private const val DEFAULT_MOVES = 15

        /**
         * "Patlatma" flash animasyonunun süresi (ms). Bu süre boyunca board
         * eski halinde kalır ve [GameUiState.explodingPositions] UI
         * tarafından vurgulanır; sonrasında gravity+refill uygulanmış yeni
         * board yerleşir. Değer çok kısa olursa değişiklik hissedilmez, çok
         * uzunsa oynanış yavaşlar — 430 ms patlamayı okunur kılan dengeli
         * bir aralıktır.
         */
        private const val EXPLOSION_ANIM_MS: Long = 430L
        private const val JOKER_SWAP_ANIM_MS: Long = 720L
        private const val JOKER_SHUFFLE_ANIM_MS: Long = 760L

        /**
         * ViewModel üretim fabrikası. `viewModel(factory = GameViewModel.Factory)`
         * çağrısıyla Compose içinden kullanılır. Application ve
         * SavedStateHandle'ı CreationExtras üzerinden alır.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = requireNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as WordCrushApp
                val handle = createSavedStateHandle()
                val dictionary = app.dictionaryRepository
                GameViewModel(
                    application = app,
                    savedStateHandle = handle,
                    engine = WordCrushEngine(),
                    dictionaryRepository = dictionary,
                    validateWord = ValidateWordUseCase(dictionary),
                    scoreCalculator = WordScoreCalculator(),
                    comboAnalyzer = ComboAnalyzer(dictionary),
                    historyRepository = app.gameHistoryRepository,
                    jokerInventoryRepository = app.jokerInventoryRepository,
                    jokerEngine = JokerEngine(),
                    availableWordCounter = AvailableWordCounter(),
                    ensurePlayableBoard = EnsurePlayableBoardUseCase(
                        engine = WordCrushEngine(),
                        counter = AvailableWordCounter()
                    )
                )
            }
        }

    }
}
