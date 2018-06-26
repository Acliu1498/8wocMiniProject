import io.requery.*

@Entity
interface Language : Persistable {

    @get:Key
    var name: String
    var identifier: String
    var direction: String
}

@Entity
interface Book : Persistable {

    @get:Key
    @get:Generated
    var id: Int
    var name: String

    @get:ForeignKey
    var fkLanguageName: String
}

@Entity
interface Chapter : Persistable {

    @get:Key
    @get:Generated
    var id: Int
    var num: Int

    @get:ForeignKey
    var fkBookId: Int
}
