package xin.vanilla.entity

import kotlinx.serialization.Serializable
import lombok.Getter
import lombok.Setter
import lombok.experimental.Accessors


@Setter
@Getter
@Accessors(chain = true)
@Serializable
class TestEntity {
    var name: String = ""
    var age: Long = 0

    constructor()

    constructor(name: String, age: Long) {
        this.name = name
        this.age = age
    }

    override fun toString(): String {
        return "TestEntity(name='$name', age=$age)"
    }

}

class TestEntities {
    var entities: List<TestEntity> = emptyList()
    var page: Long = 0
    var tag: String = ""
    override fun toString(): String {
        return "TestEntities(entities=$entities, page=$page, tag='$tag')"
    }

}