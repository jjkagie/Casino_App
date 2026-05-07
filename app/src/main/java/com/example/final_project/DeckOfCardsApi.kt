package com.example.final_project

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/deck/new/shuffle/")
    suspend fun getShuffledDeck(@Query("deck_count") num: Int): Deck

    @GET("api/deck/{deckId}/draw/")
    suspend fun getCards(
        @Path("deckId") id: String,
        @Query("count") num: Int
    ): Deck

    @GET("api/deck/{deckId}/shuffle/")
    suspend fun shuffleDeck(@Path("deckId") id: String): Deck
}

object RetrofitClient {

    private const val BASE_URL = "https://deckofcardsapi.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

data class Deck(
    val success: Boolean,
    @SerializedName("deck_id") val id: String,
    val shuffled: Boolean?,
    val cards: MutableList<Card>?,
    val remaining: Int
)

data class Card(
    val value: String,
    val suit: String,
    val image: String
)