package social.alone.server.picture

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile
import social.alone.server.RestDocsConfiguration
import social.alone.server.picture.service.PictureResizeService
import java.io.FileInputStream
import javax.imageio.ImageIO


@RunWith(SpringRunner::class)
@WebMvcTest(PictureController::class, secure = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "server.money-whip.com", uriPort = 443)
@Import(RestDocsConfiguration::class)
class PictureControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var imageUploader: ImageUploader

    @MockBean
    private lateinit var imageDownloader: ImageDownloader

    @MockBean
    private lateinit var pictureResizeService: PictureResizeService

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)


    @Test
    fun imageCreate() {
        val multipartFile = MockMultipartFile("test.jpg", FileInputStream(createTempFile()))
        val image = Picture("imageUrl")
        given(
                imageUploader.upload(any(MultipartFile::class.java))
        ).willReturn(image)

        val perform = mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart("/pictures")
                        .file("file", multipartFile.bytes)
        )

        perform.andExpect(status().isOk)
        perform.andDo(MockMvcRestDocumentation.document("picture-create"))
    }

    @Test
    fun imageRead() {
        val picture = Picture("imageUrl")
        picture.id = "asd"

        val perform = mockMvc.perform(
                MockMvcRequestBuilders.get("/pictures/" + picture.id)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        )

        perform.andDo(MockMvcResultHandlers.print())
        perform.andExpect(status().isOk)
    }

//    @Test
//    fun imageResize() {
//        val bufferedImage = ImageIO.read(FileInputStream(createTempFile("test", ".jpg")))
//        val picture = Picture("imageUrl")
//        picture.id = "1234"
//        given(
//                imageDownloader.download(any(String::class.java))
//        ).willReturn(bufferedImage)
//        val perform = mockMvc.perform(
//                MockMvcRequestBuilders.get("/pictures/" + picture.id + "/image/hd")
//                        .contentType(MediaType.IMAGE_JPEG)
//        )
//
//        perform.andExpect(status().isOk)
//    }
}
