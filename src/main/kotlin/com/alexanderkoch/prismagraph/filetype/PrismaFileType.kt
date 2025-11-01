package com.alexanderkoch.prismagraph.filetype

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * File Type Definition für Prisma Schema Dateien
 */
class PrismaFileType : LanguageFileType(PrismaLanguage.INSTANCE) {
    
    override fun getName(): String = "Prisma Schema"
    
    override fun getDescription(): String = "Prisma Schema File"
    
    override fun getDefaultExtension(): String = "prisma"
    
    override fun getIcon(): Icon? {
        return try {
            // Try SVG icon first (better quality, supports all sizes)
            IconLoader.getIcon("/icons/prisma.svg", PrismaFileType::class.java)
        } catch (e: Exception) {
            try {
                // Fallback to PNG if SVG not available
                IconLoader.getIcon("/icons/prisma.png", PrismaFileType::class.java)
            } catch (e2: Exception) {
                null // Fallback zu Standard-Icon
            }
        }
    }
    
    companion object {
        @JvmField
        val INSTANCE = PrismaFileType()
    }
}