package com.alexanderkoch.prismagraph.icons

import com.alexanderkoch.prismagraph.filetype.PrismaFileType
import com.intellij.ide.IconProvider
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import javax.swing.Icon

/**
 * Icon Provider für Prisma Schema Dateien
 */
class PrismaIconProvider : IconProvider() {
    
    private val prismaIcon: Icon? by lazy {
        // Try SVG icon first (better quality, supports all sizes)
        try {
            IconLoader.getIcon("/icons/prisma.svg", PrismaIconProvider::class.java)
        } catch (e: Exception) {
            try {
                // Fallback to PNG if SVG not available
                IconLoader.getIcon("/icons/prisma.png", PrismaIconProvider::class.java)
            } catch (e2: Exception) {
                // Verwende ein Standard-Icon als Fallback
                try {
                    IconLoader.getIcon("/general/add.png", PrismaIconProvider::class.java)
                } catch (e3: Exception) {
                    null
                }
            }
        }
    }
    
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiFile && element.fileType == PrismaFileType.INSTANCE) {
            return prismaIcon
        }
        return null
    }
}