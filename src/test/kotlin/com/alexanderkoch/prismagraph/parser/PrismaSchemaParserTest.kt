package com.alexanderkoch.prismagraph.parser

import com.alexanderkoch.prismagraph.model.RelationType
import org.junit.Test
import org.junit.Assert.*

class PrismaSchemaParserTest {
    
    private val parser = PrismaSchemaParser()
    
    @Test
    fun testParseSimpleModel() {
        val schema = """
            model User {
              id    Int    @id @default(autoincrement())
              email String @unique
              name  String?
            }
        """.trimIndent()
        
        val result = parser.parseContent(schema, "test.prisma")
        
        assertEquals(1, result.models.size)
        val user = result.models[0]
        assertEquals("User", user.name)
        assertEquals(3, user.fields.size)
        
        val idField = user.fields.find { it.name == "id" }
        assertNotNull(idField)
        assertEquals("Int", idField!!.type)
        assertFalse(idField.isOptional)
        
        val nameField = user.fields.find { it.name == "name" }
        assertNotNull(nameField)
        assertTrue(nameField!!.isOptional)
    }
    
    @Test
    fun testParseRelation() {
        val schema = """
            model User {
              id    Int    @id @default(autoincrement())
              posts Post[]
            }
            
            model Post {
              id       Int  @id @default(autoincrement())
              authorId Int
              author   User @relation(fields: [authorId], references: [id])
            }
        """.trimIndent()
        
        val result = parser.parseContent(schema, "test.prisma")
        
        assertEquals(2, result.models.size)
        assertEquals(2, result.relations.size)
        
        val userPostsRelation = result.relations.find { it.fromModel == "User" && it.toModel == "Post" }
        assertNotNull(userPostsRelation)
        assertEquals(RelationType.ONE_TO_MANY, userPostsRelation!!.relationType)
        
        val postAuthorRelation = result.relations.find { it.fromModel == "Post" && it.toModel == "User" }
        assertNotNull(postAuthorRelation)
        assertEquals(RelationType.ONE_TO_ONE, postAuthorRelation!!.relationType)
    }
    
    @Test
    fun testParseEnum() {
        val schema = """
            enum Role {
              USER
              ADMIN
              MODERATOR
            }
        """.trimIndent()
        
        val result = parser.parseContent(schema, "test.prisma")
        
        assertEquals(1, result.enums.size)
        val role = result.enums[0]
        assertEquals("Role", role.name)
        assertEquals(3, role.values.size)
        assertTrue(role.values.contains("USER"))
        assertTrue(role.values.contains("ADMIN"))
        assertTrue(role.values.contains("MODERATOR"))
    }
    
    @Test
    fun testParseArrayField() {
        val schema = """
            model Post {
              id   Int    @id
              tags String[]
            }
        """.trimIndent()
        
        val result = parser.parseContent(schema, "test.prisma")
        
        val post = result.models[0]
        val tagsField = post.fields.find { it.name == "tags" }
        assertNotNull(tagsField)
        assertTrue(tagsField!!.isArray)
        assertEquals("String", tagsField.type)
    }
}