package social.alone.server.ping

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import social.alone.server.auth.oauth2.user.CurrentUser
import social.alone.server.controller.BaseController
import social.alone.server.post.domain.Post
import social.alone.server.user.domain.User
import social.alone.server.user.repository.UserRepository
import java.util.*

@RestController
class PingController(
        val pingCreateService: PingCreateService,
        val pingSearchService: PingSearchService,
        val userRepository: UserRepository
) : BaseController() {


    @PutMapping("/posts/{postId}/pings")
    @PreAuthorize("hasRole('USER')")
    fun createPing(
            @CurrentUser currentUser: User,
            @PathVariable("postId") optionalPost: Optional<Post>,
            @RequestBody request: PingCreateRequest
    ): ResponseEntity<*> {
        if (!optionalPost.isPresent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build<Any>()
        }
        val optionalReceiver = userRepository.findById(request.receiverId)
        if (!optionalReceiver.isPresent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build<Any>()
        }
        val ping = pingCreateService.create(currentUser, optionalReceiver.get(), optionalPost.get())
        return ResponseEntity.ok(ping)
    }

    @GetMapping("/posts/{postId}/pings")
    fun getPings(
            @PathVariable("postId") postId: String,
            pageable: Pageable
    ): Page<PingView> {
        println(postId)
        val pings = pingSearchService.findByPostId(postId, pageable)
        return pings.map { it.view() }
    }

}