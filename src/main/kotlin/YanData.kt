package com.github

import com.github.XXYan.serializeToYanCode
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.forEach
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.text
import java.sql.SQLException


class YanData(id: Long) : Table<YanEntity>(id.toString()) {


    val name = text("name").bindTo { it.name }
    val head = text("head").bindTo { it.head }
    val yan = text("yan").bindTo { it.yan }
    val title = text("title").bindTo { it.title }
    val yanCode = text("yanCode").bindTo { it.yanCode }

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

        fun updateDataVersion(id: Long): String {
            val table = YanData(id)
            table.createTableIfNotExist()
            table.tryAlterColumn(table.title.name, table.title.sqlType.typeName)
            table.tryAlterColumn(table.yanCode.name, table.yanCode.sqlType.typeName)
            var countNewYanCode = 0
            var countEmptyYanCode = 0
            database.sequenceOf(table).forEach { yanEntity ->
                if (yanEntity.yanCode == "") {
                    val targetYanText = yanEntity.yan
                    val newYanCode = yanEntity.yan.deserializeMiraiCode().serializeToYanCode()
                    if (newYanCode != "") {
                        database.update(table) {
                            set(table.yanCode, newYanCode)
                            where {
                                it.yan eq targetYanText
                            }
                        }
                        countNewYanCode++
                    } else {
                        database.delete(table) {
                            it.yan eq targetYanText
                        }
                        countEmptyYanCode++
                    }
                }
            }
            val message = "updateDataVersion done for ${table.tableName}, countNewYanCode = $countNewYanCode, countEmptyYanCode = $countEmptyYanCode"
            XXYan.logger.info(message)
            return message
        }

        fun getSequence(id: Long): EntitySequence<YanEntity, YanData> {
            val table = YanData(id)
            table.createTableIfNotExist()
            return database.sequenceOf(table)
        }
    }
}
