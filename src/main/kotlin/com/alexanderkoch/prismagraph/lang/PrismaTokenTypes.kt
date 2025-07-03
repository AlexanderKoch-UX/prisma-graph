package com.alexanderkoch.prismagraph.lang

import com.alexanderkoch.prismagraph.filetype.PrismaLanguage
import com.intellij.psi.tree.IElementType

/**
 * Token Types für Prisma Schema Dateien
 */
object PrismaTokenTypes {
    @JvmField
    val IDENTIFIER = IElementType("IDENTIFIER", PrismaLanguage.INSTANCE)
    
    @JvmField
    val COMMENT = IElementType("COMMENT", PrismaLanguage.INSTANCE)
    
    @JvmField
    val STRING = IElementType("STRING", PrismaLanguage.INSTANCE)
    
    @JvmField
    val KEYWORD = IElementType("KEYWORD", PrismaLanguage.INSTANCE)
}