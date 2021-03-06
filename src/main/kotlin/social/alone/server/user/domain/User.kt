package social.alone.server.user.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.Builder
import lombok.Setter
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.UpdateTimestamp
import social.alone.server.auth.oauth2.user.OAuth2UserInfo
import social.alone.server.interest.Interest
import social.alone.server.push.domain.FcmToken
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
        @Embedded
        var profile: Profile) {

    @Id
    @Column(unique = true, columnDefinition = "VARCHAR(64)")
    @GeneratedValue(generator = "uuid", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    val id: String = ""

    @CreationTimestamp
    val createdAt: LocalDateTime? = null

    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null
        protected set


    @NotNull
    @Email
    @Column(nullable = false)
    @Setter
    @JsonIgnore
    var email: String? = null

    @JsonIgnore
    var password: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    var provider: AuthProvider? = null

    @JsonIgnore
    var providerId: String? = null

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    val roles: MutableSet<UserRole> = hashSetOf(UserRole.USER)


    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.EAGER)
    @JoinTable(name = "user_interest", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "interest_id")])
    var interests: MutableSet<Interest> = HashSet()

    @OneToMany
    @JsonIgnore
    var fcmTokens: MutableSet<FcmToken> = HashSet()

    val isAdmin: Boolean
        get() = this.roles.contains(UserRole.ADMIN)

    @Builder
    constructor(email: String, password: String? = null, profile: Profile) : this(profile) {
        this.email = email
        this.password = password
        this.provider = AuthProvider.local
    }

    constructor(oAuth2UserInfo: OAuth2UserInfo, provider: AuthProvider) : this(Profile(oAuth2UserInfo.name)) {

        this.roles.add(UserRole.USER)
        this.interests = HashSet()
        this.email = oAuth2UserInfo.email
        this.provider = provider
        this.providerId = oAuth2UserInfo.id
    }

    fun setInterests(interests: HashSet<Interest>) {
        this.interests = interests
    }


    fun updateByOauth(oAuth2UserInfo: OAuth2UserInfo) {
        this.profile.updateName(oAuth2UserInfo.name)
    }

}
