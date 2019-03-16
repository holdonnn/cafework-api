package social.alone.server.auth;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder
@Getter
public class SignUpRequestDto {

  @NotEmpty
  @Size(max = 20)
  public String name;

  @NotEmpty
  @Size(min = 6, max = 20)
  public String password;

  @NotEmpty
  @Email
  public String email;
}