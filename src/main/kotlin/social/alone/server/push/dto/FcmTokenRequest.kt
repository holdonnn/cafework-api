package social.alone.server.push.dto

import javax.validation.Valid
import javax.validation.constraints.NotEmpty

class FcmTokenRequest {

    @Valid
    @NotEmpty
    var fcmToken: String? = null
        internal set
}
