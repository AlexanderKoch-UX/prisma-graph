package com.alexanderkoch.prismagraph.parser

import com.alexanderkoch.prismagraph.model.*
import java.io.File

/**
 * Parser für Prisma Schema Dateien
 */
class PrismaSchemaParser {
    
    fun parseFile(file: File): PrismaSchema {
        val content = file.readText()
        return parseContent(content, file.absolutePath)
    }
    
    fun parseContent(content: String, sourceFile: String): PrismaSchema {
        val lines = content.lines()
        val models = mutableListOf<PrismaModel>()
        val enums = mutableListOf<PrismaEnum>()
        val relations = mutableListOf<PrismaRelation>()
        
        var currentIndex = 0
        
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            
            when {
                line.startsWith("model ") -> {
                    val (model, nextIndex) = parseModel(lines, currentIndex, sourceFile)
                    models.add(model)
                    currentIndex = nextIndex
                }
                line.startsWith("enum ") -> {
                    val (enum, nextIndex) = parseEnum(lines, currentIndex, sourceFile)
                    enums.add(enum)
                    currentIndex = nextIndex
                }
                else -> currentIndex++
            }
        }
        
        // Extrahiere Relationen aus den Models
        models.forEach { model ->
            relations.addAll(extractRelationsFromModel(model))
        }
        
        return PrismaSchema(
            models = models,
            relations = relations,
            enums = enums,
            sourceFiles = setOf(sourceFile)
        )
    }
    
    private fun parseModel(lines: List<String>, startIndex: Int, sourceFile: String): Pair<PrismaModel, Int> {
        val modelLine = lines[startIndex].trim()
        val modelName = modelLine.substringAfter("model ").substringBefore(" {").trim()
        
        val fields = mutableListOf<PrismaField>()
        val attributes = mutableListOf<String>()
        var currentIndex = startIndex + 1
        
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            
            if (line == "}") {
                break
            }
            
            if (line.isNotEmpty() && !line.startsWith("//")) {
                when {
                    line.startsWith("@@") -> {
                        attributes.add(line)
                    }
                    else -> {
                        val field = parseField(line)
                        if (field != null) {
                            fields.add(field)
                        }
                    }
                }
            }
            
            currentIndex++
        }
        
        val model = PrismaModel(
            name = modelName,
            fields = fields,
            attributes = attributes,
            sourceFile = sourceFile
        )
        
        return Pair(model, currentIndex + 1)
    }
    
    private fun parseField(line: String): PrismaField? {
        try {
            val parts = line.trim().split(Regex("\\s+"))
            if (parts.size < 2) return null
            
            val name = parts[0]
            var type = parts[1]
            
            val isOptional = type.endsWith("?")
            val isArray = type.endsWith("[]") || type.contains("[]")
            
            // Bereinige den Typ
            type = type.removeSuffix("?").removeSuffix("[]")
            
            // Prüfe ob es eine Relation ist (beginnt mit Großbuchstabe und ist kein primitiver Typ)
            val isRelation = type.first().isUpperCase() && !isPrimitiveType(type)
            
            val attributes = mutableListOf<String>()
            var relationName: String? = null
            val relationFields = mutableListOf<String>()
            val relationReferences = mutableListOf<String>()
            
            // Parse Attribute
            if (parts.size > 2) {
                val attributePart = parts.drop(2).joinToString(" ")
                val attributeMatches = Regex("@\\w+(?:\\([^)]*\\))?").findAll(attributePart)
                
                attributeMatches.forEach { match ->
                    val attribute = match.value
                    attributes.add(attribute)
                    
                    // Parse Relation-Attribute
                    if (attribute.startsWith("@relation")) {
                        val relationMatch = Regex("@relation\\(([^)]*)\\)").find(attribute)
                        relationMatch?.let { relMatch ->
                            val params = relMatch.groupValues[1]
                            
                            // Parse name
                            val nameMatch = Regex("name:\\s*\"([^\"]+)\"").find(params)
                            relationName = nameMatch?.groupValues?.get(1)
                            
                            // Parse fields
                            val fieldsMatch = Regex("fields:\\s*\\[([^]]+)\\]").find(params)
                            fieldsMatch?.let { fm ->
                                relationFields.addAll(
                                    fm.groupValues[1].split(",").map { it.trim().removeSurrounding("\"") }
                                )
                            }
                            
                            // Parse references
                            val referencesMatch = Regex("references:\\s*\\[([^]]+)\\]").find(params)
                            referencesMatch?.let { rm ->
                                relationReferences.addAll(
                                    rm.groupValues[1].split(",").map { it.trim().removeSurrounding("\"") }
                                )
                            }
                        }
                    }
                }
            }
            
            return PrismaField(
                name = name,
                type = type,
                isOptional = isOptional,
                isArray = isArray,
                isRelation = isRelation,
                relationName = relationName,
                relationFields = relationFields,
                relationReferences = relationReferences,
                attributes = attributes
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun parseEnum(lines: List<String>, startIndex: Int, sourceFile: String): Pair<PrismaEnum, Int> {
        val enumLine = lines[startIndex].trim()
        val enumName = enumLine.substringAfter("enum ").substringBefore(" {").trim()
        
        val values = mutableListOf<String>()
        var currentIndex = startIndex + 1
        
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            
            if (line == "}") {
                break
            }
            
            if (line.isNotEmpty() && !line.startsWith("//")) {
                values.add(line)
            }
            
            currentIndex++
        }
        
        val enum = PrismaEnum(
            name = enumName,
            values = values,
            sourceFile = sourceFile
        )
        
        return Pair(enum, currentIndex + 1)
    }
    
    private fun extractRelationsFromModel(model: PrismaModel): List<PrismaRelation> {
        val relations = mutableListOf<PrismaRelation>()
        
        model.getRelationFields().forEach { field ->
            val relationType = when {
                field.isArray -> RelationType.ONE_TO_MANY
                field.relationFields.isNotEmpty() -> RelationType.ONE_TO_ONE
                else -> RelationType.ONE_TO_ONE
            }
            
            val relation = PrismaRelation(
                name = field.relationName,
                fromModel = model.name,
                toModel = field.type,
                fromField = field.name,
                toField = field.relationReferences.firstOrNull(),
                relationType = relationType,
                sourceFile = model.sourceFile
            )
            
            relations.add(relation)
        }
        
        return relations
    }
    
    private fun isPrimitiveType(type: String): Boolean {
        return type in setOf(
            "String", "Int", "Float", "Boolean", "DateTime", "Json", "Bytes",
            "Decimal", "BigInt", "Unsupported"
        )
    }
}