package com.alexanderkoch.prismagraph.actions

import com.alexanderkoch.prismagraph.service.PrismaSchemaService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

/**
 * Action zum Aktualisieren des Prisma Graphs
 */
class RefreshGraphAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val schemaService = project.service<PrismaSchemaService>()
        schemaService.scanForSchemaFiles()
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}