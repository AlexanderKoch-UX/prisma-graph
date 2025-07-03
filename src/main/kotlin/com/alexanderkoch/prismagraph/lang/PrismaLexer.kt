package com.alexanderkoch.prismagraph.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * Einfacher Lexer für Prisma Schema Dateien
 * Minimale Implementierung für Dateierkennung
 */
class PrismaLexer : LexerBase() {
    
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var currentOffset = 0
    private var currentTokenType: IElementType? = null
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentOffset = startOffset
        advance()
    }
    
    override fun getState(): Int = 0
    
    override fun getTokenType(): IElementType? = currentTokenType
    
    override fun getTokenStart(): Int = currentOffset
    
    override fun getTokenEnd(): Int {
        if (currentOffset >= endOffset) return endOffset
        
        var end = currentOffset
        while (end < endOffset && !Character.isWhitespace(buffer[end])) {
            end++
        }
        return end
    }
    
    override fun advance() {
        if (currentOffset >= endOffset) {
            currentTokenType = null
            return
        }
        
        val char = buffer[currentOffset]
        currentTokenType = when {
            Character.isWhitespace(char) -> {
                skipWhitespace()
                com.intellij.psi.TokenType.WHITE_SPACE
            }
            char == '/' && currentOffset + 1 < endOffset && buffer[currentOffset + 1] == '/' -> {
                skipLineComment()
                PrismaTokenTypes.COMMENT
            }
            else -> {
                skipToNextWhitespace()
                PrismaTokenTypes.IDENTIFIER
            }
        }
    }
    
    private fun skipWhitespace() {
        while (currentOffset < endOffset && Character.isWhitespace(buffer[currentOffset])) {
            currentOffset++
        }
    }
    
    private fun skipLineComment() {
        while (currentOffset < endOffset && buffer[currentOffset] != '\n') {
            currentOffset++
        }
    }
    
    private fun skipToNextWhitespace() {
        while (currentOffset < endOffset && !Character.isWhitespace(buffer[currentOffset])) {
            currentOffset++
        }
    }
    
    override fun getBufferSequence(): CharSequence = buffer
    
    override fun getBufferEnd(): Int = endOffset
}