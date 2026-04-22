package com.saffet.wordcrushmobile.domain.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [TrieWordDictionary] için birim testler. `contains` ve `hasPrefix`
 * davranışının sözlükteki kelimelerle tutarlı olduğunu doğrular.
 */
class TrieWordDictionaryTest {

    @Test
    fun `contains - sozlukteki kelime true doner`() {
        val dict = TrieWordDictionary(listOf("kalem", "kale", "kart"))
        assertTrue(dict.contains("kalem"))
        assertTrue(dict.contains("kale"))
    }

    @Test
    fun `contains - prefix olarak gecen ama kendisi kelime olmayan girdi false`() {
        val dict = TrieWordDictionary(listOf("kalem"))
        assertFalse(dict.contains("kale"))
        assertFalse(dict.contains("k"))
    }

    @Test
    fun `contains - sozlukte olmayan kelime false`() {
        val dict = TrieWordDictionary(listOf("kalem"))
        assertFalse(dict.contains("xyz"))
    }

    @Test
    fun `contains - bos string false`() {
        val dict = TrieWordDictionary(listOf("kalem"))
        assertFalse(dict.contains(""))
    }

    @Test
    fun `hasPrefix - gecerli onek true`() {
        val dict = TrieWordDictionary(listOf("kalem", "kale", "kart"))
        assertTrue(dict.hasPrefix("k"))
        assertTrue(dict.hasPrefix("ka"))
        assertTrue(dict.hasPrefix("kal"))
        assertTrue(dict.hasPrefix("kar"))
    }

    @Test
    fun `hasPrefix - gecersiz onek false`() {
        val dict = TrieWordDictionary(listOf("kalem"))
        assertFalse(dict.hasPrefix("xyz"))
        assertFalse(dict.hasPrefix("kz"))
    }

    @Test
    fun `hasPrefix - tam kelime uzunlugu da true`() {
        val dict = TrieWordDictionary(listOf("kalem"))
        assertTrue(dict.hasPrefix("kalem"))
    }

    @Test
    fun `size - tekrar edenler bir kere sayilir`() {
        val dict = TrieWordDictionary(listOf("kalem", "kalem", "kale"))
        assertEquals(2, dict.size)
    }

    @Test
    fun `bos kelimeler eklenmez`() {
        val dict = TrieWordDictionary(listOf("kalem", "", "kale"))
        assertEquals(2, dict.size)
    }

    @Test
    fun `turkce karakterlerle calisir`() {
        val dict = TrieWordDictionary(listOf("çiçek", "şapka", "ğül"))
        assertTrue(dict.contains("çiçek"))
        assertTrue(dict.hasPrefix("çi"))
        assertTrue(dict.hasPrefix("şa"))
    }
}
