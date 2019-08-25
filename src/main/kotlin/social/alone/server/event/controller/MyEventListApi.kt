package social.alone.server.event.controller


import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import social.alone.server.auth.oauth2.user.CurrentUser
import social.alone.server.event.service.EventSearchService
import social.alone.server.event.type.EventQueryParams
import social.alone.server.user.domain.User
import javax.validation.Valid

@Controller
@RequestMapping(value = ["/api/events/my"])
class MyEventListApi(private val eventSearchService: EventSearchService) {

    @GetMapping("/upcoming")
    fun upcoming(
            @PageableDefault(sort = ["startedAt"], direction = Sort.Direction.ASC) pageable: Pageable,
            @CurrentUser user: User?
    ): ResponseEntity<*> {
        if (user == null) {
            return ResponseEntity.badRequest().build<Any>()
        }

        val page = this.eventSearchService
                .findAllMyUpcomingEvents(user, pageable)
        return ResponseEntity.ok(page)
    }

    @GetMapping("/past")
    fun past(
            @PageableDefault(sort = ["startedAt"], direction = Sort.Direction.ASC) pageable: Pageable,
            @CurrentUser user: User?,
            @Valid eventQueryParams: EventQueryParams
    ): ResponseEntity<*> {

        if (user == null) {
            return ResponseEntity.badRequest().build<Any>()
        }

        val page = this.eventSearchService
                .findAllMyPastEvents(user, pageable)
        return ResponseEntity.ok(page)
    }

}
