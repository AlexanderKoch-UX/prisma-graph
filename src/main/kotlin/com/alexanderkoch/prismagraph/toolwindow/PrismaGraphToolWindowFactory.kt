package com.alexanderkoch.prismagraph.toolwindow

import com.alexanderkoch.prismagraph.service.PrismaSchemaService
import com.alexanderkoch.prismagraph.service.SchemaChangeListener
import com.alexanderkoch.prismagraph.ui.PrismaGraphPanel
import com.alexanderkoch.prismagraph.model.PrismaSchema
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Factory für das Prisma Graph Tool Window
 */
class PrismaGraphToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {
            val prismaGraphToolWindow = PrismaGraphToolWindow(project)
            val content = ContentFactory.getInstance().createContent(
                prismaGraphToolWindow.getContent(),
                "",
                false
            )
            toolWindow.contentManager.addContent(content)
        } catch (e: Exception) {
            // Fallback: Simple label if main content fails
            val fallbackLabel = JLabel("Prisma Graph Plugin loaded - Error: ${e.message}")
            val fallbackContent = ContentFactory.getInstance().createContent(
                fallbackLabel,
                "",
                false
            )
            toolWindow.contentManager.addContent(fallbackContent)
        }
    }
    
    override fun shouldBeAvailable(project: Project): Boolean {
        return true // Always show the tool window
    }
}

/**
 * Das Hauptfenster für die Prisma Graph Visualisierung - SynthWave '84 Theme
 */
class PrismaGraphToolWindow(private val project: Project) : SchemaChangeListener {
    
    private val schemaService = project.service<PrismaSchemaService>()
    private val graphPanel = PrismaGraphPanel()
    
    // SynthWave '84 Color Palette
    private val synthDarkBg = Color(0x0D1117)
    private val synthPurple = Color(0x9D4EDD)
    private val synthPink = Color(0xFF006E)
    private val synthCyan = Color(0x00F5FF)
    private val synthYellow = Color(0xFFBE0B)
    private val synthDarkCard = Color(0x1A1B26)
    private val synthText = Color(0xE2E8F0)
    private val synthSubtext = Color(0x9CA3AF)
    
    private val statusLabel = JLabel("READY").apply {
        foreground = synthCyan
        font = Font("Consolas", Font.BOLD, 12)
    }
    private val modelCountLabel = JLabel("MODELS: 0").apply {
        foreground = synthPurple
        font = Font("Consolas", Font.BOLD, 12)
    }
    private val relationCountLabel = JLabel("RELATIONS: 0").apply {
        foreground = synthPink
        font = Font("Consolas", Font.BOLD, 12)
    }
    
    private val mainPanel = JPanel(BorderLayout()).apply {
        background = synthDarkBg
    }
    
    init {
        setupUI()
        setupListeners()
        
        // Initiales Schema laden
        val initialSchema = schemaService.getCombinedSchema()
        updateGraph(initialSchema)
    }
    
    private fun setupUI() {
        // Toolbar
        val toolbar = createToolbar()
        mainPanel.add(toolbar, BorderLayout.NORTH)
        
        // Graph Panel in ScrollPane with SynthWave styling
        val scrollPane = JScrollPane(graphPanel).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            background = synthDarkBg
            viewport.background = synthDarkBg
            border = BorderFactory.createLineBorder(synthPurple, 1)
            
            // Style scrollbars
            horizontalScrollBar.apply {
                background = synthDarkCard
                foreground = synthPurple
            }
            verticalScrollBar.apply {
                background = synthDarkCard
                foreground = synthPurple
            }
        }
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        // Status Bar
        val statusBar = createStatusBar()
        mainPanel.add(statusBar, BorderLayout.SOUTH)
    }
    
    private fun createToolbar(): JPanel {
        val toolbar = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            background = synthDarkCard
        }
        
        // Create SynthWave styled buttons
        fun createSynthButton(text: String, color: Color): JButton {
            return JButton(text).apply {
                background = synthDarkBg
                foreground = color
                font = Font("Consolas", Font.BOLD, 11)
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                )
                isOpaque = true
                isFocusPainted = false
                
                // Hover effect
                addMouseListener(object : java.awt.event.MouseAdapter() {
                    override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                        background = Color(color.red, color.green, color.blue, 30)
                    }
                    override fun mouseExited(e: java.awt.event.MouseEvent?) {
                        background = synthDarkBg
                    }
                })
            }
        }
        
        // Refresh Button
        val refreshButton = createSynthButton("REFRESH", synthCyan).apply {
            addActionListener { refreshSchema() }
        }
        
        // Fit to Screen Button
        val fitButton = createSynthButton("FIT", synthPurple).apply {
            addActionListener {
                graphPanel.dispatchEvent(java.awt.event.KeyEvent(
                    graphPanel, 
                    java.awt.event.KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    java.awt.event.KeyEvent.VK_F,
                    'F'
                ))
            }
        }
        
        // Reset View Button
        val resetButton = createSynthButton("RESET", synthPink).apply {
            addActionListener {
                graphPanel.dispatchEvent(java.awt.event.KeyEvent(
                    graphPanel,
                    java.awt.event.KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    java.awt.event.KeyEvent.VK_R,
                    'R'
                ))
            }
        }
        
        toolbar.add(refreshButton)
        toolbar.add(Box.createHorizontalStrut(8))
        toolbar.add(fitButton)
        toolbar.add(Box.createHorizontalStrut(8))
        toolbar.add(resetButton)
        toolbar.add(Box.createHorizontalGlue())
        
        return toolbar
    }
    
    private fun createStatusBar(): JPanel {
        val statusBar = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, synthPurple),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            )
            background = synthDarkCard
        }
        
        statusBar.add(statusLabel)
        statusBar.add(Box.createHorizontalStrut(20))
        statusBar.add(modelCountLabel)
        statusBar.add(Box.createHorizontalStrut(20))
        statusBar.add(relationCountLabel)
        statusBar.add(Box.createHorizontalGlue())
        
        // Help Text with SynthWave styling
        val helpLabel = JLabel("RIGHT-CLICK + DRAG: PAN | SCROLL: ZOOM | R: RESET | F: FIT").apply {
            foreground = synthSubtext
            font = Font("Consolas", Font.PLAIN, 10)
        }
        statusBar.add(helpLabel)
        
        return statusBar
    }
    
    private fun setupListeners() {
        // Schema Change Listener registrieren
        project.messageBus.connect().subscribe(
            PrismaSchemaService.SCHEMA_CHANGED_TOPIC,
            this
        )
    }
    
    private fun refreshSchema() {
        statusLabel.text = "UPDATING SCHEMA..."
        SwingUtilities.invokeLater {
            schemaService.scanForSchemaFiles()
            statusLabel.text = "SCHEMA UPDATED"
        }
    }
    
    private fun updateGraph(schema: PrismaSchema) {
        SwingUtilities.invokeLater {
            graphPanel.updateSchema(schema)
            updateStatusLabels(schema)
        }
    }
    
    private fun updateStatusLabels(schema: PrismaSchema) {
        modelCountLabel.text = "MODELS: ${schema.models.size}"
        relationCountLabel.text = "RELATIONS: ${schema.relations.size}"
        
        if (schema.models.isEmpty()) {
            statusLabel.text = "NO PRISMA SCHEMA FILES FOUND"
        } else {
            statusLabel.text = "SCHEMA LOADED (${schema.sourceFiles.size} FILES)"
        }
    }
    
    override fun schemaChanged(schema: PrismaSchema) {
        updateGraph(schema)
    }
    
    fun getContent(): JComponent {
        return mainPanel
    }
}