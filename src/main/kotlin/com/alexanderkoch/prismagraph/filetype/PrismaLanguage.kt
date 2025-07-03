package com.alexanderkoch.prismagraph.filetype

import com.intellij.lang.Language

/**
 * Language Definition für Prisma Schema
 */
class PrismaLanguage : Language("PrismaGraph") {
    
    companion object {
        @JvmField
        val INSTANCE = PrismaLanguage()
    }
}