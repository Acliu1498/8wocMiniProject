import io.requery.sql.platform.SQLite
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.http.GET
import java.io.File
import java.net.URL
import java.nio.charset.Charset


/**
 * Alex Liu, Jennifer Huang - Wycliffe Associates - 6/20/2018 - 8wocMiniChallenge
 * this class manages connection with Door43 api
 * the functions here take care of accessing all of the json data
 */
class Door43Manager(private val api: Api = Api()){
    private val callResponse = api.getLanguage("") // gets the main call response
    private val retrieved = callResponse.execute() // executes the call response
    private val selectedBooks: ArrayList<bookResponse> = ArrayList() // and arraylist of all the bookResponses

    /**
     * this function gets the number of languages in the api
     */
    fun getLanguages(): Map<String, Pair<String, String>>{
        // list to be returned
        var ret = HashMap<String, Pair<String, String>>()
        // checks if had be recieved
        if(retrieved.isSuccessful){
            for (language in retrieved.body().languages){
                if (getBooks(language.title).isNotEmpty()) {
                    ret[language.title] = Pair(language.identifier,  language.direction)
                }
            }
        }
        return ret
    }

    /**
     * This function gets the books from the door43 api given a language
     * returns as a list of strings
     */
    fun getBooks(language: String): List<String>{
        // books to be returned
        selectedBooks.clear()
        val books = ArrayList<String>()
        try {
            // finds the correct resource
            var languageResponse: LanguageResponse? = retrieved.body().languages.find { it.title == language }
            var resource: ResourceResponse? = languageResponse!!.resources.find { it.subject == "Bible" }
            // searches each book in the resource
            for (book in resource!!.projects) {
                if (book.formats.find { it.format == "text/usfm" } != null) {
                    // adds to the selected books and the return
                    selectedBooks.add(book)
                    books.add(book.title)
                }
            }
        }catch (e: NullPointerException){}
        return books
    }

    /**
     * this function gets the number of chapters in a given book
     */
    fun getChapters(book: String): List<String>{
        // chapters that will be returned
        val chapters = ArrayList<String>()
        // the text in the USFM file
        val text = getUSFM(book)!!.lines()
        // start number of chapters
        var numChapters = 1
        // goes through lines
        for(line in text){
            // if line contains a \c then it is a chapter
            if (line.contains("\\c")){
                // adds chapter number to the string
                chapters.add(numChapters.toString())
                numChapters++
            }
        }
        return chapters
    }

    /**
     * function to get verses from a selection
     */
    fun getVerses(book: String, chapter: String): List<String> {

        // verses that will be returned
        val verses = ArrayList<String>()

        // the text in the USFM file
        var text = getUSFM(book)
        val nextChapter = if (text.contains("\\c ${chapter.toInt() + 1}")){
            text.indexOf("\\c ${chapter.toInt() + 1}")
        } else {
            text.length
        }
        text = text.substring(text.indexOf("\\c $chapter"), nextChapter)
        // start number of verses
        var numVerses = 1
        // goes through lines
        for (line in text.lines()) {
            if(line.contains("\\v")){
                verses.add(numVerses.toString())
                numVerses++
            }
        }
        return verses
    }

    /**
     * helper function that gets the USFM file given a book
     * selected books must already have data
     */
    fun getUSFM(book: String): String{
        var text = ""
        try {
            // searches through the formats of the selected books
            for(format in selectedBooks.find { it.title == book }!!.formats){
                // if the format is of type USFM then return
                if(format.format == "text/usfm"){
                    text = URL(format.url).readText()
                }
            }
        } catch (e: NullPointerException){}
        return text

    }

}