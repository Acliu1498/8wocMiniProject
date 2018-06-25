import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Alex Liu, Jennifer Huang - Wycliffe Associates - 6/20/2018 - 8wocMiniChallenge
 * Interface to interact directly with the door43 api
 */
interface Door43Api {
    @GET("/v3/catalog.json")
    fun getLang(@Query("languages") lang: String): Call<CatalogResponse>
}