package com.alexanderkoch.prismagraph.model

/**
 * Repräsentiert ein Prisma Model aus dem Schema
 */
data class PrismaModel(
    val name: String,
    val fields: List<PrismaField>,
    val attributes: List<String> = emptyList(),
    val sourceFile: String
) {
    fun getRelationFields(): List<PrismaField> {
        return fields.filter { it.isRelation }
    }
    
    fun getScalarFields(): List<PrismaField> {
        return fields.filter { !it.isRelation }
    }
}

/**
 * Repräsentiert ein Feld in einem Prisma Model
 */
data class PrismaField(
    val name: String,
    val type: String,
    val isOptional: Boolean = false,
    val isArray: Boolean = false,
    val isRelation: Boolean = false,
    val relationName: String? = null,
    val relationFields: List<String> = emptyList(),
    val relationReferences: List<String> = emptyList(),
    val attributes: List<String> = emptyList()
) {
    val displayType: String
        get() = buildString {
            append(type)
            if (isArray) append("[]")
            if (isOptional) append("?")
        }
}

/**
 * Repräsentiert eine Relation zwischen zwei Models
 */
data class PrismaRelation(
    val name: String?,
    val fromModel: String,
    val toModel: String,
    val fromField: String,
    val toField: String?,
    val relationType: RelationType,
    val sourceFile: String
)

enum class RelationType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY
}

/**
 * Repräsentiert das komplette Prisma Schema
 */
data class PrismaSchema(
    val models: List<PrismaModel>,
    val relations: List<PrismaRelation>,
    val enums: List<PrismaEnum> = emptyList(),
    val sourceFiles: Set<String>
) {
    fun getModelByName(name: String): PrismaModel? {
        return models.find { it.name == name }
    }
    
    fun getRelationsForModel(modelName: String): List<PrismaRelation> {
        return relations.filter { it.fromModel == modelName || it.toModel == modelName }
    }
}

/**
 * Repräsentiert ein Prisma Enum
 */
data class PrismaEnum(
    val name: String,
    val values: List<String>,
    val sourceFile: String
)