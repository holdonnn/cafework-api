package social.alone.server.post.domain

import org.hibernate.annotations.GenericGenerator
import social.alone.server.picture.Picture
import social.alone.server.user.domain.User
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "post")
class Post(@ManyToOne var author: User, @NotNull var text: String, @OneToOne var picture: Picture) {
    fun isAuthor(user: User): Boolean {
        return this.author.id == user.id
    }

    @Id
    @Column(unique = true, columnDefinition = "VARCHAR(64)")
    @GeneratedValue(generator = "uuid", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    val id: String = ""

    val createdAt: LocalDateTime = LocalDateTime.now()

    var updatedAt: LocalDateTime = LocalDateTime.now()

}
