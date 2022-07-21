package com.github

import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.entity.Entity
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.text
import java.sql.SQLException


class YanData(id: Long) : Table<YanEntity>(id.toString()) {


    val name = text("name").bindTo { it.name }
    val head = text("head").bindTo { it.head }
    val yan = text("yan").bindTo { it.yan }
    val title = text("title").bindTo { it.title }
    companion object {
        private val database = Database.connect("jdbc:sqlite:file:${XXYan.resolveDataFile("yan.db")}")

        private fun YanData.tryAlterColumn(columnName: String, type: String) {
            try {
                database.useConnection {
                    val sql =
                        """
                            ALTER TABLE "${this@tryAlterColumn.tableName}" ADD COLUMN "$columnName" $type
                        """.trimIndent()
                    it.createStatement().execute(sql)
                }
                XXYan.logger.info("alterColumn done for ${this@tryAlterColumn.tableName}.$columnName")
            } catch (e: SQLException) {
                // alterColumn fail for ${this@tryAlterColumn.tableName}.$columnName, Most likely it already exists
                // do nothing
            }
        }

        private fun YanData.createTableIfNotExist() {
            database.useConnection {
                it.createStatement().execute(
                    """
                        CREATE TABLE IF NOT EXISTs "${this@createTableIfNotExist.tableName}"(
                        name TEXT,
                        head TEXT,
                        yan TEXT,
                        title TEXT
                        )                    
                    """.trimIndent()
                )
            }
        }


        fun getSequence(id: Long): EntitySequence<YanEntity, YanData> {
            val table = YanData(id)
            table.createTableIfNotExist()
            table.tryAlterColumn(table.title.name, table.title.sqlType.typeName)
            return database.sequenceOf(table)
        }
    }
}
