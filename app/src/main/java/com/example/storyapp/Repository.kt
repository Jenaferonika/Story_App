import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import com.example.storyapp.User.UserModel
import com.example.storyapp.User.UserPreference
import com.example.storyapp.addStory.addStoryResponse
import com.example.storyapp.api.ApiService
import com.example.storyapp.db.StoryDatabase
import com.example.storyapp.detail.DetailResponse
import com.example.storyapp.login.LoginResponse
import com.example.storyapp.register.RegisterResponse
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.story.StoryResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class Repository private constructor(
    private val userPreference: UserPreference,
    private var api: ApiService,
    private val database: StoryDatabase
) {
    private val _listStory = MutableStateFlow<List<ListStoryItem>>(emptyList())
    val listStory = _listStory.asStateFlow()

    fun authenticateUser(email: String, password: String): Flow<LoginResponse> = flow {
        try {
            val loginResponse = api.login(email, password)
            storeUserSession(UserModel(email, loginResponse.loginResult.token, isLogin = true))
            emit(loginResponse)
        } catch (exception: Exception) {
            throw exception
        }
    }.flowOn(Dispatchers.IO)

    fun userRegister(
        name: String,
        email: String,
        password: String
    ): Flow<RegisterResponse> = flow {
        try {
            val registrationResult = api.register(name, email, password)
            emit(registrationResult)
        } catch (exception: Exception) {
            exception.printStackTrace()
            emit(RegisterResponse(error = true, message = exception.message ?: "Registration failed"))
        }
    }.flowOn(Dispatchers.IO)

    fun updateApiService(apiService: ApiService) {
        this.api = apiService
    }

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = RemoteMediator(database, api),
            pagingSourceFactory = {
                database.storyDao().getAllStories()
            }
        ).liveData
    }

    suspend fun getDetailStories(id: String): Flow<DetailResponse> = flow {
        try {
            val response = api.getDetailStories(id)
            emit(response)
        } catch (e: Exception) {
            throw Exception(e.message ?: "An error occurred while fetching story details")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addStory(
        imageFile: File,
        description: String,
        lat: Double?,
        lon: Double?
    ): Flow<addStoryResponse> = flow {
        emit(addStoryResponse(error = false, message = "Loading..."))  // Menampilkan loading sementara

        // Pastikan lat dan lon tidak null dengan memberikan nilai default jika null
        val validLat = lat?.toString() ?: "0.0"  // Atur nilai default jika null
        val validLon = lon?.toString() ?: "0.0"  // Atur nilai default jika null

        // Persiapkan RequestBody untuk data yang akan dikirimkan
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImg = imageFile.asRequestBody("image/jpg".toMediaType())  // Pastikan MIME type sesuai
        val requestLat = validLat.toRequestBody("text/plain".toMediaType())  // Pastikan validLat digunakan
        val requestLon = validLon.toRequestBody("text/plain".toMediaType())  // Pastikan validLon digunakan

        // Membuat MultipartBody untuk file
        val bodyMultipart = MultipartBody.Part.createFormData(
            "photo",  // Sesuaikan dengan nama parameter di API
            imageFile.name,
            requestImg
        )

        try {
            // Panggil API untuk menambahkan story
            val responseSuccess = api.addStory(bodyMultipart, requestBody, requestLat, requestLon)
            emit(responseSuccess)  // Mengirimkan hasil sukses

            // Jika response berhasil, ambil stories terbaru
            if (!responseSuccess.error) {
                val updatedStoriesResponse = api.getStories()
                val updatedStories = updatedStoriesResponse.listStory

                // Update database lokal dengan data terbaru
                database.withTransaction {
                    database.storyDao().deleteAll()  // Opsional: hapus data lama
                    // Masukkan data stories yang terbaru ke database
                    database.storyDao().storyInsert(updatedStories.map {
                        ListStoryItem(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            lat = it.lat,
                            lon = it.lon,
                            photoUrl = it.photoUrl,
                            createdAt = it.createdAt
                        )
                    })
                }
            }
        } catch (e: HttpException) {
            // Menangani error jika terjadi exception dari API
            val errorBody = e.response()?.errorBody()?.string()
            val responseError = Gson().fromJson(errorBody, addStoryResponse::class.java)
            emit(responseError)  // Mengirimkan hasil error
        } catch (e: Exception) {
            emit(addStoryResponse(error = true, message = e.message ?: "Unknown error occurred"))  // Error umum jika tidak ada koneksi atau lainnya
        }
    }


    fun getLocation(): Flow<StoryResponse> {
        return flow {
            val response = api.getStories() // API call
            emit(response)
        }
    }

    internal suspend fun storeUserSession(userData: UserModel) {
        userPreference.setUserSession(userData)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var sharedInstance: Repository? = null

        fun getInstance(preferences: UserPreference, api: ApiService, database: StoryDatabase): Repository =
            sharedInstance ?: synchronized(this) {
                sharedInstance ?: Repository(preferences, api, database).also { sharedInstance = it }
            }
    }
}
