package com.school.hei.service;

import static java.io.File.createTempFile;

import com.school.hei.endpoint.event.model.SendEmailRequested;
import com.school.hei.entity.PostConstants;
import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.mail.Email;
import com.school.hei.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {

  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(SendEmailRequested event) {
    var originalFile = bucketComponent.download(event.getBucketKey());

    var grayFile = toGrayscale(originalFile);

    var grayBucketKey = "grayscale/" + UUID.randomUUID() + "-" + originalFile.getName();
    bucketComponent.upload(grayFile, grayBucketKey);

    var presignedUri = bucketComponent.presign(grayBucketKey, PostConstants.IMAGE_LINK_EXPIRATION);

    var email =
        new Email(
            new InternetAddress(event.getTo()),
            List.of(),
            List.of(),
            "Votre image en noir et blanc est prête",
            "Voici le lien de téléchargement (valable 30 minutes) : " + presignedUri,
            List.of());
    mailer.accept(email);
  }

  private File toGrayscale(File original) {
    try {
      var originalImage = ImageIO.read(original);
      var grayImage =
          new BufferedImage(
              originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      var graphics = grayImage.getGraphics();
      graphics.drawImage(originalImage, 0, 0, null);
      graphics.dispose();

      var format = extension(original.getName());
      var outputFile = createTempFile("grayscale", "." + format);
      ImageIO.write(grayImage, format, outputFile);
      return outputFile;
    } catch (Exception e) {
      throw new com.school.hei.exception.ImageProcessingException(
          "Failed to convert image to grayscale", e);
    }
  }

  private String extension(String filename) {
    var parts = filename.split("\\.");
    return parts.length > 1 ? parts[parts.length - 1] : "png";
  }
}
