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
    
    private val prismaIcon: Icon by lazy {
        // Fallback zu einem Standard-Icon falls das Prisma-Icon nicht gefunden wird
        try {
            IconLoader.getIcon("/icons/prisma.png", PrismaIconProvider::class.java)
        } catch (e: Exception) {
            // Verwende ein Standard-Icon als Fallback
            IconLoader.getIcon("/general/add.png", PrismaIconProvider::class.java)
        }
    }
    
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiFile && element.fileType == PrismaFileType.INSTANCE) {
            return prismaIcon
        }
        return null
    }
}