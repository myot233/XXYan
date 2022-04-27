package com.github

import me.liuwj.ktorm.entity.Entity

interface YanEntity:Entity<YanEntity> {
    var name:String
    var head:String
    var yan:String

    companion object: Entity.Factory<YanEntity>() {
    }
}