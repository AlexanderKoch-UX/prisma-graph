package com.alexanderkoch.prismagraph.lang

import com.alexanderkoch.prismagraph.filetype.PrismaLanguage
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Parser Definition für Prisma Schema Dateien
 * Minimale Implementierung für Dateierkennung
 */
class PrismaParserDefinition : ParserDefinition {
    
    companion object {
        val FILE = IFileElementType(PrismaLanguage.INSTANCE)
    }
    
    override fun createLexer(project: Project?): Lexer {
        return PrismaLexer()
    }
    
    override fun createParser(project: Project?): PsiParser {
        return PrismaParser()
    }
    
    override fun getFileNodeType(): IFileElementType {
        return FILE
    }
    
    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(PrismaTokenTypes.COMMENT)
    }
    
    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(PrismaTokenTypes.STRING)
    }
    
    override fun getWhitespaceTokens(): TokenSet {
        return TokenSet.create(TokenType.WHITE_SPACE)
    }
    
    override fun createElement(node: ASTNode?): PsiElement {
        return PrismaElement(node!!)
    }
    
    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return PrismaFile(viewProvider)
    }
}