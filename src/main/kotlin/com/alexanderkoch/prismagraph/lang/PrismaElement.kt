package com.alexanderkoch.prismagraph.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * PSI Element für Prisma Schema Dateien
 */
class PrismaElement(node: ASTNode) : ASTWrapperPsiElement(node)