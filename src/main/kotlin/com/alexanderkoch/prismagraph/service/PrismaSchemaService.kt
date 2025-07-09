package com.alexanderkoch.prismagraph.service

import com.alexanderkoch.prismagraph.model.PrismaSchema
import com.alexanderkoch.prismagraph.parser.PrismaSchemaParser
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.Topic
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Service zur Verwaltung und Überwachung von Prisma Schema Dateien
 */
@Service(Service.Level.PROJECT)
class PrismaSchemaService(private val project: Project) : com.intellij.openapi.Disposable {
    
    private val parser = PrismaSchemaParser()
    private val schemaCache = ConcurrentHashMap<String, PrismaSchema>()
    private val messageBus: MessageBus = project.messageBus
    
    private var fileListener: com.intellij.openapi.vfs.newvfs.BulkFileListener? = null
    
    init {
        setupFileWatcher()
        scanForSchemaFiles()
    }
    
    /**
     * Gibt das kombinierte Schema aller gefundenen .prisma Dateien zurück
     */
    fun getCombinedSchema(): PrismaSchema {
        val allModels = mutableListOf<com.alexanderkoch.prismagraph.model.PrismaModel>()
        val allRelations = mutableListOf<com.alexanderkoch.prismagraph.model.PrismaRelation>()
        val allEnums = mutableListOf<com.alexanderkoch.prismagraph.model.PrismaEnum>()
        val allSourceFiles = mutableSetOf<String>()
        
        schemaCache.values.forEach { schema ->
            allModels.addAll(schema.models)
            allRelations.addAll(schema.relations)
            allEnums.addAll(schema.enums)
            allSourceFiles.addAll(schema.sourceFiles)
        }
        
        return PrismaSchema(
            models = allModels,
            relations = allRelations,
            enums = allEnums,
            sourceFiles = allSourceFiles
        )
    }
    
    /**
     * Lädt eine spezifische Schema-Datei neu
     */
    fun reloadSchemaFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists() && file.extension == "prisma") {
                val schema = parser.parseFile(file)
                schemaCache[filePath] = schema
                notifySchemaChanged()
            }
        } catch (e: Exception) {
            // Log error but don't crash
            println("Error reloading schema file $filePath: ${e.message}")
        }
    }
    
    /**
     * Entfernt eine Schema-Datei aus dem Cache
     */
    fun removeSchemaFile(filePath: String) {
        schemaCache.remove(filePath)
        notifySchemaChanged()
    }
    
    /**
     * Scannt das Projekt nach .prisma Dateien
     */
    fun scanForSchemaFiles() {
        schemaCache.clear()
        
        val projectPath = project.basePath ?: return
        scanDirectory(File(projectPath))
        
        notifySchemaChanged()
    }
    
    private fun scanDirectory(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory && !file.name.startsWith(".") && file.name != "node_modules" -> {
                    scanDirectory(file)
                }
                file.isFile && file.extension == "prisma" -> {
                    try {
                        val schema = parser.parseFile(file)
                        schemaCache[file.absolutePath] = schema
                    } catch (e: Exception) {
                        println("Error parsing schema file ${file.absolutePath}: ${e.message}")
                    }
                }
            }
        }
    }
    
    private fun setupFileWatcher() {
        fileListener = object : com.intellij.openapi.vfs.newvfs.BulkFileListener {
            override fun after(events: List<com.intellij.openapi.vfs.newvfs.events.VFileEvent>) {
                events.forEach { event ->
                    when (event) {
                        is com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent -> {
                            if (event.file?.extension == "prisma") {
                                reloadSchemaFile(event.file!!.path)
                            }
                        }
                        is com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent -> {
                            if (event.file.extension == "prisma") {
                                removeSchemaFile(event.file.path)
                            }
                        }
                        is com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent -> {
                            if (event.file.extension == "prisma") {
                                reloadSchemaFile(event.file.path)
                            }
                        }
                        is com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent -> {
                            if (event.file.extension == "prisma") {
                                removeSchemaFile(event.oldParent.path + "/" + event.file.name)
                                reloadSchemaFile(event.file.path)
                            }
                        }
                    }
                }
            }
        }
        
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, fileListener!!)
    }
    
    private fun notifySchemaChanged() {
        messageBus.syncPublisher(SCHEMA_CHANGED_TOPIC).schemaChanged(getCombinedSchema())
    }
    
    override fun dispose() {
        // Message bus connections are automatically disposed when the disposable is disposed
        fileListener = null
    }
    
    companion object {
        val SCHEMA_CHANGED_TOPIC = Topic.create("PrismaSchemaChanged", SchemaChangeListener::class.java)
    }
}

/**
 * Interface für Schema-Änderungsbenachrichtigungen
 */
interface SchemaChangeListener {
    fun schemaChanged(schema: PrismaSchema)
}