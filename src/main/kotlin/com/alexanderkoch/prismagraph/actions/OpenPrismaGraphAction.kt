package com.alexanderkoch.prismagraph.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action zum Öffnen des Prisma Graph Tool Windows
 */
class OpenPrismaGraphAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("PrismaGraph")
        
        if (toolWindow != null) {
            toolWindow.activate(null)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}