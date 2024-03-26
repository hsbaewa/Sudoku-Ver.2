package kr.co.hs.sudoku

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.gson.Gson
import org.junit.Before
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayOutputStream

abstract class FirebaseTest {
    @Before
    open fun onBefore() {
        runCatching {
            FirebaseApp.getInstance()
        }.getOrElse {

            val outputStream = ByteArrayOutputStream()
            javaClass.classLoader?.getResource("google-services.json")?.openStream()?.use {
                it.copyTo(outputStream)
            }
            val json = String(outputStream.toByteArray())
            val googleServices = Gson().fromJson(json, GoogleServices::class.java)
            val projectId = googleServices.project_info?.project_id ?: ""

            val packageName = RuntimeEnvironment.getApplication().packageName
            val applicationId = googleServices.client
                ?.find { it.client_info?.android_client_info?.get("package_name") == packageName }
                ?.client_info?.mobilesdk_app_id
                ?: ""

            FirebaseApp.initializeApp(
                RuntimeEnvironment.getApplication(),
                FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId(applicationId)
                    .build()
            )
        }
    }

    private data class GoogleServices(
        val project_info: ProjectInfo?,
        val client: List<Client>?
    )

    private data class ProjectInfo(
        val project_number: String?,
        val firebase_url: String?,
        val project_id: String?,
        val storage_bucket: String?
    )

    private data class Client(
        val client_info: ClientInfo?
    )

    private data class ClientInfo(
        val mobilesdk_app_id: String?,
        val android_client_info: Map<String, String>?
    )
}