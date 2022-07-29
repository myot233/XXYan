package com.github

import org.ktorm.entity.Entity

interface YanEntity: Entity<YanEntity> {
    var name:String
    var head:String
    var yan:String
    var yanCode:String
    var title:String

    companion object: Entity.Factory<YanEntity>() {
    }
}