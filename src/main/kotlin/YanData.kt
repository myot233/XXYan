package com.github

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.EntitySequence
import me.liuwj.ktorm.entity.forEach
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.text


class YanData(id: Long) : Table<YanEntity>(id.toString()) {


    val name = text("name").bindTo { it.name }
    val head = text("head").bindTo { it.head }
    val yan = text("yan").bindTo { it.yan }

    companion object {
        fun getSequence(id: Long): EntitySequence<YanEntity, YanData> {
            val database = Database.connect("jdbc:sqlite:data\\yan.db")
            database.useConnection {
                it.createStatement().execute(
                    """
                        CREATE TABLE IF NOT EXISTs "${id}"(
                        name TEXT,
                        head TEXT,
                        yan TEXT
                        )                    
                    """.trimIndent()
                )
            }
            return database.sequenceOf(YanData(id))
        }
    }
}
