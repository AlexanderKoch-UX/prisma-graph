package com.alexanderkoch.prismagraph.lang

import com.alexanderkoch.prismagraph.filetype.PrismaFileType
import com.alexanderkoch.prismagraph.filetype.PrismaLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

/**
 * PSI File für Prisma Schema Dateien
 */
class PrismaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, PrismaLanguage.INSTANCE) {
    
    override fun getFileType(): FileType = PrismaFileType.INSTANCE
    
    override fun toString(): String = "Prisma File"
}