import io.requery.meta.EntityModel
import io.requery.sql.KotlinConfiguration
import org.sqlite.SQLiteDataSource

class DbManager(){

    init {
        val configuration = KotlinConfiguration(dataSource = SQLiteDataSource(), model = Models.DEFAULT)
    }

}