// data class for storing the catalog
class CatalogResponse(val languages: List<LanguageResponse>)

// data class for the languages
class LanguageResponse(
        val identifier: String,
        val direction: String,
        val title: String,
        val resources: List<ResourceResponse>
)

// data class for the resources
class ResourceResponse(
        val projects: List<bookResponse>,
        val subject: String
)

// data class for the books
class bookResponse(
        val formats: List<formatResponse>,
        val title: String
)

// data class
class formatResponse(
        val format: String,
        val url: String
)

