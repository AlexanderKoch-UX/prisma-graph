package com.alexanderkoch.prismagraph.ui

import com.alexanderkoch.prismagraph.model.*
import java.awt.*
import java.awt.event.*
import java.awt.geom.Rectangle2D
import javax.swing.*
import kotlin.math.*

/**
 * Panel für die graphische Darstellung des Prisma Schemas
 */
class PrismaGraphPanel : JPanel() {
    
    private var schema: PrismaSchema = PrismaSchema(emptyList(), emptyList(), emptyList(), emptySet())
    private var modelPositions = mutableMapOf<String, Point>()
    private var selectedModel: String? = null
    private var draggedModel: String? = null
    private var dragOffset = Point(0, 0)
    private var scale = 1.0
    private var panOffset = Point(0, 0)
    private var lastMousePos = Point(0, 0)
    private var isPanning = false
    private var enforceMinimumDistance = true  // Toggle for minimum distance enforcement
    
    // Styling - SynthWave '84 Theme
    private val modelWidth = 200
    private val modelHeaderHeight = 30
    private val fieldHeight = 20
    private val modelPadding = 10
    private val modelSpacing = 50
    private val minimumDistance = 80  // Minimum distance between model centers
    
    // SynthWave '84 Color Palette
    private val synthDarkBg = Color(0x0D1117)           // Deep dark background
    private val synthPurple = Color(0x9D4EDD)           // Bright purple
    private val synthPink = Color(0xFF006E)             // Hot pink
    private val synthCyan = Color(0x00F5FF)             // Electric cyan
    private val synthYellow = Color(0xFFBE0B)           // Neon yellow
    private val synthOrange = Color(0xFF5722)           // Neon orange
    private val synthDarkCard = Color(0x1A1B26)         // Dark card background
    private val synthBorder = Color(0x2D3748)           // Subtle border
    private val synthText = Color(0xE2E8F0)             // Light text
    private val synthSubtext = Color(0x9CA3AF)          // Muted text
    private val synthGlow = Color(0x7C3AED)             // Glow effect
    
    // Theme colors
    private val headerColor = synthPurple
    private val modelColor = synthDarkCard
    private val borderColor = synthBorder
    private val selectedColor = synthCyan
    private val relationColor = synthPink
    private val textColor = synthText
    private val fieldTypeColor = synthSubtext
    
    init {
        background = synthDarkBg
        isOpaque = true
        
        setupMouseListeners()
        setupKeyListeners()
        
        isFocusable = true
        requestFocusInWindow()
    }
    
    fun updateSchema(newSchema: PrismaSchema) {
        schema = newSchema
        layoutModels()
        repaint()
    }
    
    private fun layoutModels() {
        if (schema.models.isEmpty()) return
        
        // Clear existing positions for fresh layout
        modelPositions.clear()
        
        // Calculate grid dimensions with minimum distance consideration
        val cols = ceil(sqrt(schema.models.size.toDouble())).toInt()
        val rows = ceil(schema.models.size.toDouble() / cols).toInt()
        
        // Calculate spacing that ensures minimum distance
        val effectiveSpacing = maxOf(modelSpacing, minimumDistance)
        
        schema.models.forEachIndexed { index, model ->
            val col = index % cols
            val row = index / cols
            
            // Initial position calculation
            var x = col * (modelWidth + effectiveSpacing) + effectiveSpacing
            var y = row * (calculateModelHeight(model) + effectiveSpacing) + effectiveSpacing
            
            // Ensure minimum distance from all existing models (if enabled)
            var position = Point(x, y)
            if (enforceMinimumDistance) {
                position = ensureMinimumDistance(position, model.name)
            }
            
            modelPositions[model.name] = position
        }
    }
    
    /**
     * Ensures the given position maintains minimum distance from all existing models
     */
    private fun ensureMinimumDistance(proposedPosition: Point, currentModelName: String): Point {
        var adjustedPosition = Point(proposedPosition.x, proposedPosition.y)
        var attempts = 0
        val maxAttempts = 100 // Prevent infinite loops
        
        while (attempts < maxAttempts) {
            var hasConflict = false
            
            // Check distance to all existing models
            for ((modelName, existingPosition) in modelPositions) {
                if (modelName == currentModelName) continue
                
                val distance = calculateDistance(adjustedPosition, existingPosition)
                if (distance < minimumDistance) {
                    // Calculate adjustment vector
                    val dx = adjustedPosition.x - existingPosition.x
                    val dy = adjustedPosition.y - existingPosition.y
                    val currentDistance = sqrt((dx * dx + dy * dy).toDouble())
                    
                    if (currentDistance > 0) {
                        // Normalize and scale to minimum distance
                        val scale = minimumDistance / currentDistance
                        adjustedPosition.x = existingPosition.x + (dx * scale).toInt()
                        adjustedPosition.y = existingPosition.y + (dy * scale).toInt()
                    } else {
                        // If positions are identical, offset by minimum distance
                        adjustedPosition.x += minimumDistance
                        adjustedPosition.y += minimumDistance / 2
                    }
                    
                    hasConflict = true
                    break
                }
            }
            
            if (!hasConflict) break
            attempts++
        }
        
        // Ensure position is not negative
        adjustedPosition.x = maxOf(adjustedPosition.x, modelSpacing)
        adjustedPosition.y = maxOf(adjustedPosition.y, modelSpacing)
        
        return adjustedPosition
    }
    
    /**
     * Calculates the distance between two points
     */
    private fun calculateDistance(point1: Point, point2: Point): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }
    
    /**
     * Ensures minimum distance during drag operations with smoother behavior
     */
    private fun ensureMinimumDistanceForDrag(proposedPosition: Point, currentModelName: String): Point {
        var adjustedPosition = Point(proposedPosition.x, proposedPosition.y)
        
        // Check distance to all other models
        for ((modelName, existingPosition) in modelPositions) {
            if (modelName == currentModelName) continue
            
            val distance = calculateDistance(adjustedPosition, existingPosition)
            if (distance < minimumDistance && distance > 0) {
                // Calculate push-away vector
                val dx = adjustedPosition.x - existingPosition.x
                val dy = adjustedPosition.y - existingPosition.y
                val currentDistance = sqrt((dx * dx + dy * dy).toDouble())
                
                // Normalize and scale to minimum distance
                val scale = minimumDistance / currentDistance
                adjustedPosition.x = existingPosition.x + (dx * scale).toInt()
                adjustedPosition.y = existingPosition.y + (dy * scale).toInt()
            }
        }
        
        // Ensure position is not negative
        adjustedPosition.x = maxOf(adjustedPosition.x, modelSpacing)
        adjustedPosition.y = maxOf(adjustedPosition.y, modelSpacing)
        
        return adjustedPosition
    }
    
    private fun calculateModelHeight(model: PrismaModel): Int {
        return modelHeaderHeight + (model.fields.size * fieldHeight) + (modelPadding * 2)
    }
    
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Anwenden von Zoom und Pan
        g2d.translate(panOffset.x, panOffset.y)
        g2d.scale(scale, scale)
        
        // Zeichne Relationen zuerst (damit sie unter den Models sind)
        drawRelations(g2d)
        
        // Zeichne Models
        schema.models.forEach { model ->
            drawModel(g2d, model)
        }
    }
    
    private fun drawModel(g2d: Graphics2D, model: PrismaModel) {
        val position = modelPositions[model.name] ?: return
        val height = calculateModelHeight(model)
        
        val isSelected = selectedModel == model.name
        val borderColor = if (isSelected) selectedColor else this.borderColor
        val borderWidth = if (isSelected) 3 else 1
        
        // SynthWave glow effect for selected models
        if (isSelected) {
            drawGlowEffect(g2d, position.x - 5, position.y - 5, modelWidth + 10, height + 10, selectedColor)
        }
        
        // Visual indicator for minimum distance constraint
        if (draggedModel == model.name && enforceMinimumDistance) {
            drawMinimumDistanceIndicator(g2d, position.x + modelWidth/2, position.y + height/2)
        }
        
        // Model-Hintergrund with gradient
        drawGradientBackground(g2d, position.x, position.y, modelWidth, height)
        
        // Model-Border with neon effect
        g2d.color = borderColor
        g2d.stroke = BasicStroke(borderWidth.toFloat())
        g2d.drawRoundRect(position.x, position.y, modelWidth, height, 12, 12)
        
        if (isSelected) {
            // Additional glow border
            g2d.color = Color(selectedColor.red, selectedColor.green, selectedColor.blue, 100)
            g2d.stroke = BasicStroke(2f)
            g2d.drawRoundRect(position.x - 2, position.y - 2, modelWidth + 4, height + 4, 14, 14)
        }
        
        // Header with gradient
        drawHeaderGradient(g2d, position.x, position.y, modelWidth, modelHeaderHeight)
        
        // Model Name with glow effect
        g2d.color = Color.WHITE
        g2d.font = Font("Consolas", Font.BOLD, 14) // Monospace font for retro feel
        val headerMetrics = g2d.fontMetrics
        val nameX = position.x + (modelWidth - headerMetrics.stringWidth(model.name)) / 2
        val nameY = position.y + (modelHeaderHeight + headerMetrics.ascent) / 2
        
        // Text glow effect
        drawTextWithGlow(g2d, model.name, nameX, nameY, Color.WHITE, synthCyan)
        
        // Fields
        g2d.font = Font("Consolas", Font.PLAIN, 11)
        val fieldMetrics = g2d.fontMetrics
        
        model.fields.forEachIndexed { index, field ->
            val fieldY = position.y + modelHeaderHeight + modelPadding + (index * fieldHeight)
            
            // Field Name
            g2d.color = textColor
            g2d.drawString(field.name, position.x + modelPadding, fieldY + fieldMetrics.ascent)
            
            // Field Type with color coding
            val typeColor = when {
                field.displayType.contains("String") -> synthYellow
                field.displayType.contains("Int") || field.displayType.contains("Float") -> synthOrange
                field.displayType.contains("Boolean") -> synthPink
                field.displayType.contains("DateTime") -> synthCyan
                else -> fieldTypeColor
            }
            g2d.color = typeColor
            val typeX = position.x + modelWidth - modelPadding - fieldMetrics.stringWidth(field.displayType)
            g2d.drawString(field.displayType, typeX, fieldY + fieldMetrics.ascent)
            
            // Relation Indicator with neon glow
            if (field.isRelation) {
                drawGlowingDot(g2d, position.x + modelPadding - 8, fieldY + 2, 6, relationColor)
            }
        }
    }
    
    private fun drawGlowEffect(g2d: Graphics2D, x: Int, y: Int, width: Int, height: Int, color: Color) {
        val composite = g2d.composite
        for (i in 5 downTo 1) {
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f * i)
            g2d.color = Color(color.red, color.green, color.blue, 50)
            g2d.fillRoundRect(x - i, y - i, width + 2 * i, height + 2 * i, 12 + i, 12 + i)
        }
        g2d.composite = composite
    }
    
    private fun drawGradientBackground(g2d: Graphics2D, x: Int, y: Int, width: Int, height: Int) {
        val gradient = GradientPaint(
            x.toFloat(), y.toFloat(), modelColor,
            x.toFloat(), (y + height).toFloat(), Color(modelColor.red, modelColor.green, modelColor.blue, 200)
        )
        g2d.paint = gradient
        g2d.fillRoundRect(x, y, width, height, 12, 12)
        g2d.paint = null
    }
    
    private fun drawHeaderGradient(g2d: Graphics2D, x: Int, y: Int, width: Int, height: Int) {
        val gradient = GradientPaint(
            x.toFloat(), y.toFloat(), headerColor,
            x.toFloat(), (y + height).toFloat(), Color(headerColor.red, headerColor.green, headerColor.blue, 150)
        )
        g2d.paint = gradient
        g2d.fillRoundRect(x, y, width, height, 12, 12)
        g2d.fillRect(x, y + height - 8, width, 8)
        g2d.paint = null
    }
    
    private fun drawTextWithGlow(g2d: Graphics2D, text: String, x: Int, y: Int, textColor: Color, glowColor: Color) {
        val composite = g2d.composite
        
        // Draw glow
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)
        g2d.color = glowColor
        for (i in 1..3) {
            g2d.drawString(text, x - i, y)
            g2d.drawString(text, x + i, y)
            g2d.drawString(text, x, y - i)
            g2d.drawString(text, x, y + i)
        }
        
        // Draw main text
        g2d.composite = composite
        g2d.color = textColor
        g2d.drawString(text, x, y)
    }
    
    private fun drawGlowingDot(g2d: Graphics2D, x: Int, y: Int, size: Int, color: Color) {
        val composite = g2d.composite
        
        // Glow effect
        for (i in 3 downTo 1) {
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f * i)
            g2d.color = Color(color.red, color.green, color.blue, 100)
            g2d.fillOval(x - i, y - i, size + 2 * i, size + 2 * i)
        }
        
        // Main dot
        g2d.composite = composite
        g2d.color = color
        g2d.fillOval(x, y, size, size)
    }
    
    /**
     * Draws a subtle indicator showing the minimum distance radius when dragging
     */
    private fun drawMinimumDistanceIndicator(g2d: Graphics2D, centerX: Int, centerY: Int) {
        val composite = g2d.composite
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f)
        
        // Draw minimum distance circle
        g2d.color = synthCyan
        g2d.stroke = BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, floatArrayOf(5f, 5f), 0f)
        g2d.drawOval(
            centerX - minimumDistance/2, 
            centerY - minimumDistance/2, 
            minimumDistance, 
            minimumDistance
        )
        
        g2d.composite = composite
    }
    
    private fun drawRelations(g2d: Graphics2D) {
        schema.relations.forEach { relation ->
            val fromPos = modelPositions[relation.fromModel]
            val toPos = modelPositions[relation.toModel]
            
            if (fromPos != null && toPos != null) {
                val fromModel = schema.getModelByName(relation.fromModel)
                val toModel = schema.getModelByName(relation.toModel)
                
                if (fromModel != null && toModel != null) {
                    // Berechne Verbindungspunkte
                    val fromHeight = calculateModelHeight(fromModel)
                    val toHeight = calculateModelHeight(toModel)
                    
                    val fromX = fromPos.x + modelWidth / 2
                    val fromY = fromPos.y + fromHeight / 2
                    val toX = toPos.x + modelWidth / 2
                    val toY = toPos.y + toHeight / 2
                    
                    // Draw glowing connection line
                    drawGlowingLine(g2d, fromX, fromY, toX, toY, relationColor)
                    
                    // Zeichne Pfeilspitze
                    drawArrowHead(g2d, fromX, fromY, toX, toY)
                    
                    // Zeichne Relation-Label with SynthWave styling
                    if (relation.name != null) {
                        val midX = (fromX + toX) / 2
                        val midY = (fromY + toY) / 2
                        
                        g2d.font = Font("Consolas", Font.BOLD, 10)
                        val labelMetrics = g2d.fontMetrics
                        val labelWidth = labelMetrics.stringWidth(relation.name)
                        val labelHeight = labelMetrics.height
                        
                        // Dark background with neon border
                        g2d.color = synthDarkCard
                        g2d.fillRoundRect(midX - labelWidth/2 - 6, midY - labelHeight/2 - 3, 
                                         labelWidth + 12, labelHeight + 6, 6, 6)
                        
                        // Neon border
                        g2d.color = relationColor
                        g2d.stroke = BasicStroke(1f)
                        g2d.drawRoundRect(midX - labelWidth/2 - 6, midY - labelHeight/2 - 3, 
                                         labelWidth + 12, labelHeight + 6, 6, 6)
                        
                        // Glowing text
                        drawTextWithGlow(g2d, relation.name, midX - labelWidth/2, midY + labelMetrics.ascent/2, 
                                        Color.WHITE, relationColor)
                    }
                }
            }
        }
    }
    
    private fun drawGlowingLine(g2d: Graphics2D, x1: Int, y1: Int, x2: Int, y2: Int, color: Color) {
        val composite = g2d.composite
        
        // Draw glow effect
        for (i in 5 downTo 1) {
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f * i)
            g2d.color = Color(color.red, color.green, color.blue, 50)
            g2d.stroke = BasicStroke((3 + i).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2d.drawLine(x1, y1, x2, y2)
        }
        
        // Draw main line
        g2d.composite = composite
        g2d.color = color
        g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2d.drawLine(x1, y1, x2, y2)
    }
    
    private fun drawArrowHead(g2d: Graphics2D, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val angle = atan2((toY - fromY).toDouble(), (toX - fromX).toDouble())
        val arrowLength = 10
        val arrowAngle = PI / 6
        
        val x1 = toX - arrowLength * cos(angle - arrowAngle)
        val y1 = toY - arrowLength * sin(angle - arrowAngle)
        val x2 = toX - arrowLength * cos(angle + arrowAngle)
        val y2 = toY - arrowLength * sin(angle + arrowAngle)
        
        g2d.drawLine(toX, toY, x1.toInt(), y1.toInt())
        g2d.drawLine(toX, toY, x2.toInt(), y2.toInt())
    }
    
    private fun setupMouseListeners() {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                requestFocusInWindow()
                lastMousePos = e.point
                
                when (e.button) {
                    MouseEvent.BUTTON1 -> {
                        val clickedModel = getModelAtPoint(e.point)
                        if (clickedModel != null) {
                            selectedModel = clickedModel
                            draggedModel = clickedModel
                            val modelPos = modelPositions[clickedModel]!!
                            dragOffset = Point(
                                ((e.x - panOffset.x) / scale - modelPos.x).toInt(),
                                ((e.y - panOffset.y) / scale - modelPos.y).toInt()
                            )
                        } else {
                            selectedModel = null
                        }
                        repaint()
                    }
                    MouseEvent.BUTTON3 -> {
                        isPanning = true
                        cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                    }
                }
            }
            
            override fun mouseReleased(e: MouseEvent) {
                draggedModel = null
                isPanning = false
                cursor = Cursor.getDefaultCursor()
            }
        })
        
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (isPanning) {
                    val dx = e.x - lastMousePos.x
                    val dy = e.y - lastMousePos.y
                    panOffset.translate(dx, dy)
                    repaint()
                } else if (draggedModel != null) {
                    val newX = ((e.x - panOffset.x) / scale - dragOffset.x).toInt()
                    val newY = ((e.y - panOffset.y) / scale - dragOffset.y).toInt()
                    val proposedPosition = Point(newX, newY)
                    
                    // Enforce minimum distance from other models (if enabled)
                    val adjustedPosition = if (enforceMinimumDistance) {
                        ensureMinimumDistanceForDrag(proposedPosition, draggedModel!!)
                    } else {
                        proposedPosition
                    }
                    modelPositions[draggedModel!!] = adjustedPosition
                    repaint()
                }
                lastMousePos = e.point
            }
        })
        
        addMouseWheelListener { e ->
            val oldScale = scale
            val scaleFactor = if (e.wheelRotation < 0) 1.1 else 0.9
            scale = (scale * scaleFactor).coerceIn(0.1, 3.0)
            
            // Zoom zum Mauszeiger
            val mouseX = e.x - panOffset.x
            val mouseY = e.y - panOffset.y
            panOffset.x += (mouseX * (oldScale - scale) / oldScale).toInt()
            panOffset.y += (mouseY * (oldScale - scale) / oldScale).toInt()
            
            repaint()
        }
    }
    
    private fun setupKeyListeners() {
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_R -> {
                        // Reset View
                        scale = 1.0
                        panOffset = Point(0, 0)
                        layoutModels()
                        repaint()
                    }
                    KeyEvent.VK_F -> {
                        // Fit to Screen
                        fitToScreen()
                    }
                }
            }
        })
    }
    
    private fun getModelAtPoint(point: Point): String? {
        val scaledPoint = Point(
            ((point.x - panOffset.x) / scale).toInt(),
            ((point.y - panOffset.y) / scale).toInt()
        )
        
        return modelPositions.entries.find { (modelName, position) ->
            val model = schema.getModelByName(modelName) ?: return@find false
            val height = calculateModelHeight(model)
            val bounds = Rectangle(position.x, position.y, modelWidth, height)
            bounds.contains(scaledPoint)
        }?.key
    }
    
    private fun fitToScreen() {
        if (modelPositions.isEmpty()) return
        
        val bounds = modelPositions.values.fold(Rectangle()) { acc, pos ->
            val model = schema.models.find { modelPositions[it.name] == pos } ?: return@fold acc
            val modelBounds = Rectangle(pos.x, pos.y, modelWidth, calculateModelHeight(model))
            if (acc.isEmpty) modelBounds else acc.union(modelBounds)
        }
        
        if (!bounds.isEmpty) {
            val scaleX = (width - 40.0) / bounds.width
            val scaleY = (height - 40.0) / bounds.height
            scale = minOf(scaleX, scaleY, 1.0)
            
            panOffset.x = ((width - bounds.width * scale) / 2 - bounds.x * scale).toInt()
            panOffset.y = ((height - bounds.height * scale) / 2 - bounds.y * scale).toInt()
            
            repaint()
        }
    }
}