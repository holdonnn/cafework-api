package social.alone.server.location;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

  final static String IMAGE_HOST = "https://alone.social";
  final static String DEFAULT_IMAGE = "https://alone.social/cafe/random/0.jpg";

  @Id
  @GeneratedValue
  private Integer id;

  public Location(
          String address,
          String name,
          Double longitude,
          Double latitude,
          String placeUrl
  ) {
    this.address = address;
    this.name = name;
    this.longitude = longitude;
    this.latitude = latitude;
    this.placeUrl = placeUrl;
    this.imageUrl = getImageUrlByName();
  }

  public static Location of(
          String address,
          String name,
          Double longitude,
          Double latitude
  ) {
    return new Location(address, name, longitude, latitude, null);
  }

  private String address;

  private String name;

  private Double longitude;

  private Double latitude;

  private String placeUrl;

  @Column(nullable = false)
  private String imageUrl = DEFAULT_IMAGE;

  private String getImageUrlByName() {
    int idx = (int)(Math.random() * 3);
    String randomImage = IMAGE_HOST + "/cafe/random/" + idx + ".jpg";

    if (this.name.contains("스타벅스")){
      return IMAGE_HOST + "/cafe/starbucks.jpg";
    }

    if (this.name.contains("이디야")){
      return IMAGE_HOST + "/cafe/ediya.jpg";
    }


    if (this.name.contains("할리스")){
      return IMAGE_HOST + "/cafe/hollys.jpg";
    }

    if (this.name.contains("폴바셋")){
      return IMAGE_HOST + "/cafe/paulbassett.jpg";
    }

    return randomImage;
  }
}
