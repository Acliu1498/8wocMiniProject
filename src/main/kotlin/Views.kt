import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.stage.Screen
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
//import sun.font.FontFamily
import tornadofx.*
import java.util.*
import kotlin.NoSuchElementException



/**
 * Alex Liu, Jennifer Huang - Wycliffe Associates - 6/20/2018 - 8wocMiniChallenge
 * These classes are the views for the application
 * The master view will be embedded with the top and center views
 * The Top view is for searching through the bible
 * THe Center view is for displaying text
 */
class MasterView: View(){
    // Embeds the top view and center view
    override val root = borderpane {
        top<TopView>()
        center<CenterView>()
    }

    init{
        with(root){
            val primaryScreenBounds = Screen.getPrimary().getVisualBounds()
            prefWidth = primaryScreenBounds.getWidth()/1.4
            prefHeight = primaryScreenBounds.getHeight()/1.4
        }
    }

}

// this view displays the selection options
class TopView: View(){
    // the controller for the application
    private val myController = MyController()
    // the available languages
    private val languages = FXCollections.observableArrayList<String>(myController.getLanguages().keys.sorted())
    // string property to hold the language info
    private val language = SimpleStringProperty("English")
    // A collection to hold the names of all the books of the Bible
    private val books = FXCollections.observableArrayList<String>(myController.getBooks("English"))
    // string property to hold book info
    private var book = SimpleStringProperty("Genesis")
    //var chapters = FXCollections.observableArrayList<String>(arrayListOf("0"))
    private var chapters = FXCollections.observableArrayList<String>(myController.getChapters(book.value))
    // string property to hold chapter info
    private var chapter = SimpleStringProperty("1")
    // verses in the selection
    private var verses = FXCollections.observableArrayList<String>(myController.getVerses(book.value, chapter.value))
    // the start of the selection verses
    private var verseStart = SimpleStringProperty()
    // the end of the selection verses
    private var verseEnd = SimpleStringProperty()
    // the centerview in the application
    private val centerView = find(CenterView::class)
    // the size of the text
    private val textSize = SimpleIntegerProperty(centerView.getFontSize().toInt())

    override val root = Form()
    // form to allow user to make a selection
    init {
        FX.locale = Locale.CHINA
        // adds a listener to update books to be in selected language
        language.addListener { obs, old, new ->
            // empties books and adds new ones
            books.clear()
            books.addAll(myController.getBooks(new))
            book.value = books[0]
        }

        // listener that finds a chapter when called
        book.addListener {  obs, old, new ->
            if(new != null) {
                // clears chapters and adds new ones
                chapters.clear()
                chapters.addAll(myController.getChapters(new))
                chapter.value = "1"
            }
        }

        chapter.addListener { obs, old, new ->
            if(new != null) {
                verses.clear()
                verses.addAll(myController.getVerses(book.value, new))
                verseStart.value = null
                verseEnd.value = null
            }
        }

        with(root) {
            fieldset {
                vbox {
                    // displays form horizontally
                    hbox(35) {
                        vbox(0){
                            label (messages["USFM_Reader"]){
                                style {
                                    fontWeight = FontWeight.EXTRA_BOLD
                                    fontSize = 25.px
                                }
                                wrapTextProperty().set(true)
                            }
                        }
                        // book field
                        vbox(5) {
                            label(messages["Book"])
                            combobox(book, books)
                            style{
                                fontFamily = "Noto Naskh Arabic UI"
                            }
                        }
                        // chapter field
                        vbox(5) {
                            label(messages["Book"])
                            combobox(chapter, chapters)
                            style{
                                fontFamily = "Noto Naskh Arabic UI"
                            }
                        }
                        // language field
                        vbox(5) {
                            label(messages["Language"])
                            combobox(language, languages)
                            style{
                                fontFamily = "Noto Naskh Arabic UI"
                            }
                        }
                        vbox(5) {
                            label(messages["From"])
                            combobox(verseStart, verses)
                        }
                        vbox(5) {
                            label (messages["To"])
                            combobox(verseEnd, verses)
                        }
                        // search field
                        button(messages["Search"]) {
                            setPrefWidth(90.00)
                            setPrefHeight(50.00)
                            style{
                                fontSize = 16.px
                                backgroundColor += c("#d49942")
                                borderWidth += box(3.px)
                            }
                            action {

                                val notNull = ObservableTransformer<String, String, String> {observable ->
                                    observable.map { it.trim() }
                                            .filter {it.value != null}
                                            .singleOrError()
                                            .onErrorResumeNext {
                                                if(it is NoSuchElementException){
                                                    Single.error(Exception("Invalid, try again"))
                                                }
                                                else{
                                                    Single.error(it)
                                                }
                                            }
                                }



                                // checks if the book chapter and language have values the update the text
                                if (book.value != null && chapter.value != null && language.value != null) {
                                    centerView.updateText(
                                            myController.search(
                                                    book.value, chapter.value, verseStart.value, verseEnd.value))
                                } else {
                                    // else notify user
                                    centerView.updateText("Invalid, try again")

                                }
                            }
                        }
                        addClass(AppStyle.wrapper)
                    }
                    hbox(20) {
                        field(messages["Text_Size"]) {
                            textfield(textSize){
                                prefWidth = 50.0
                            }
                            // field for a button to change text size
                            button(messages["Change_Text_Size"]) {
                                // when pressed updates font size
                                action {
                                    if (textSize.value != null && textSize.value > 0 && textSize.value < 100) {
                                        if (book.value != null && chapter.value != null && language.value != null) {
                                            centerView.updateText(
                                                    myController.search(
                                                            book.value, chapter.value, verseStart.value, verseEnd.value))

                                        }
                                        centerView.updateFontSize(textSize.doubleValue())
                                    }
                                    else{
                                        // else notify user
                                        centerView.updateText("Invalid, try again")
                                        textSize.value = 15
                                    }
                                }
                            }
                        }
                        field {
                            label(messages["Chapters"])
                            button(messages["Prev"]) {
                                action {
                                    // checks if the chapter can be decremented
                                    if (chapter.value.toInt() - 1 < 1) {
                                        // checks the books can be decremented
                                        if (book.value != books[0]) {
                                            // sets current book value to previous one
                                            book.value = books[books.indexOf(book.value) - 1]
                                            // sets the current chapter value to the last of the previous book
                                            chapter.value = chapters[chapters.size - 1].toString()
                                            // updates displayed text
                                            centerView.updateText(myController.search(
                                                    book.value, chapter.value, null ,null))
                                        }
                                        else{
                                            centerView.updateText("Invalid, try again")
                                        }
                                    } else {
                                        // sets the current chapter value to the previous one
                                        chapter.value = (chapter.value.toInt() - 1).toString()
                                        // updates displayed text
                                        centerView.updateText(myController.search(
                                                book.value, chapter.value, null ,null))
                                    }
                                }
                            }
                            button(messages["Next"]) {
                                action {
                                    // checks if chapter can be incremented
                                    if (chapter.value.toInt() + 1 > chapters.size) {
                                        // checks if books can be incremented
                                        if (book.value != books[books.size - 1]) {
                                            // increments book value
                                            book.value = books[books.indexOf(book.value) + 1]
                                            // resets chapter value to 1
                                            chapter.value = 1.toString()
                                            // updates text
                                            centerView.updateText(myController.search(
                                                    book.value, chapter.value, null, null))
                                        }
                                        else{
                                            centerView.updateText("Invalid, try again")
                                        }
                                    } else {
                                        // increments chapter value
                                        chapter.value = (chapter.value.toInt() + 1).toString()
                                        // updates text
                                        centerView.updateText(myController.search(
                                                book.value, chapter.value, null, null))
                                    }
                                    verses.clear()
                                    verses.addAll(myController.getVerses(book.value, chapter.value))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// this view displays the current selection
class CenterView: View(){
    override val root = VBox()
    // the chapter text
    var bibleText = SimpleStringProperty()
    // the font and size of the Bible verses
    var bibleFont = SimpleObjectProperty<Font>(Font.font("Noto Naskh Arabic UI", FontWeight.NORMAL, 15.0))

    // form to allow to read selection
    init {
        with(root) {
            scrollpane(true,false) {
                vbox {
                    // a label the contains the bible text
                    label {
                        alignmentProperty().value = Pos.CENTER
                        textProperty().bind(bibleText)
                        wrapTextProperty().set(true)
                        textAlignmentProperty().value = TextAlignment.CENTER
                        fontProperty().bind(bibleFont)
                    }
                }
                prefHeight = 1000.0
            }
            alignmentProperty().value = Pos.CENTER
            addClass(AppStyle.textWrapper)
        }

    }

    /**
     * Function that updates the text given new text
     */
    fun updateText(text: String){
        bibleText.value = text
    }

    /**
     * gets the current font size
     */
    fun getFontSize(): Double{
        return bibleFont.value.size
    }

    /**
     * updates the current font size
     */
    fun updateFontSize(size: Double){
        bibleFont.set(Font(size))
    }
}

// this class deals with any function that do not pertain to the views
// such as any function that interacts directly to the door43 manager
class MyController: Controller()  {
    // manages door43 api
    private val door43Manager: Door43Manager = Door43Manager()
    // the selected text
    private var text: String? = null
    // the selected book
    private var book: String? = null

    /**
     * function to search for the text given a book and chapter
     * able to also search for certain verses if given
     */
    fun search(book: String, chapter: String, verseStart: String?, verseEnd: String?): String{
        if(text == null || (this.book == null || this.book != book)) {
            text = door43Manager.getUSFM(book)
            this.book = book
        }
        return parseUSFM(text!!, chapter, verseStart, verseEnd)
    }

    /**
     * Function to get all the books of the Bible
     * Is pulled from books.txt in resources folder
     */
    fun getBooks(language: String): List<String>{
        return door43Manager.getBooks(language)!!
    }

    /**
     * helper function gets the number of chapters a book has
     * returns 0 as default
     */
    fun getChapters(book: String): List<String>{
        return door43Manager.getChapters(book)!!
    }

    /**
     * Temp Help function to get a list of languages
     */
    fun getLanguages(): Map<String, Pair<String, String>>{
        return door43Manager.getLanguages()
    }

    /**
     * function to get the number of verses
     */
    fun getVerses(book: String, chapter: String): List<String>{
        // if text != null gets verses from stored text
        // else goes through door43
        return door43Manager.getVerses(book, chapter)

    }

    /**
     * helper function that parses the USFM given text and a chapter
     */
    private fun parseUSFM(text: String, chapter: String, verseStart: String?, verseEnd: String?): String{
        // a list of lines in the text
        var lines = ArrayList<String>()
        // the next chapter after the one being searched
        val nextChapter = (chapter.toInt() + 1).toString()
        // the list that conatins the text to be returned
        val selection = arrayListOf<String>()
        text.lines().forEach {
            lines.add(it.trim())
        }

        // gets a sublist up to the next chapter or end of file
        lines = if (lines.indexOf("\\c $nextChapter") > 0) {
            ArrayList(lines.subList(lines.indexOf("\\c $chapter"), lines.indexOf("\\c $nextChapter")))
        } else {
            ArrayList(lines.subList(lines.indexOf("\\c $chapter"), lines.size))
        }
        if(lines.contains("\\c $chapter")) {
            // gets a sublist if only certain verses are needed
            if ((verseStart != null && verseEnd != null)) {
                // finds the start and end
                val start = lines.indexOf(lines.find { i -> i.contains("\\v $verseStart") })
                val end = lines.indexOf(lines.find { i -> i.contains("\\v $verseEnd") })
                // checks if the start is less then the end
                if (start < end) {
                    lines = ArrayList(lines.subList(start, end))
                }
            }
        }

        return parse(lines)
    }

    /**
     * helper function for parsing the usfm selection
     */
    private fun parse(lines: List<String>): String{
        val selection = ArrayList<String>()
        // looks through each line adding verses
        lines.forEach {
            // checks if line contains a verse
            if(it.contains("\\v")){
                // substring for footmarks
                var substr = ""
                // if found sets substring equal to the footmark
                if(it.contains("\\f")){
                    substr = it.substring(it.indexOf("\\f"), it.indexOf("\\f*") + 3)
                }
                if(it.contains("\\add")){
                    substr = it.substring(it.indexOf("\\add"), it.indexOf("\\add*")+5)
                }
                if(it.contains("\\pn")){
                    substr = it.substring(it.indexOf("\\pn"), it.indexOf("\\pn*")+4)
                }
                // replace the \v and the footmarks
                selection.add(it.replace("\\v", "").replace(substr, ""))
            } else if (it.contains("\\p")){
                // if contains a \p means end of paragraph
                selection.add(System.lineSeparator())
            }
        }

        // returns the full selection
        return selection.joinToString("")
    }

    fun getResourceBundle(locale: Locale): ResourceBundle{
        try {
            return ResourceBundle.getBundle("Labels", locale)
        } catch (e: MissingResourceException){}

        return ResourceBundle.getBundle("Labels", Locale("en"))
    }

}

