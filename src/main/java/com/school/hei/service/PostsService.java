package com.school.hei.service;

import static java.io.File.createTempFile;

import com.school.hei.endpoint.event.EventProducer;
import com.school.hei.endpoint.event.model.SendEmailRequested;
import com.school.hei.entity.PostConstants;
import com.school.hei.entity.Posts;
import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.repository.PostsRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class PostsService {

  private final PostsRepository postRepository;
  private final BucketComponent bucketComponent;
  private final EventProducer<SendEmailRequested> eventProducer;

  @SneakyThrows
  public Posts createPost(MultipartFile image, String name, String email) {
    var post = new Posts();
    post.setId(UUID.randomUUID());
    post.setName(name);
    post.setEmail(email);
    post.setExpirationDate(Instant.now().plus(PostConstants.IMAGE_LINK_EXPIRATION));
    postRepository.save(post);

    var tempFile = createTempFile(post.getId().toString(), null);
    image.transferTo(tempFile);
    var bucketKey = "originals/" + post.getId() + "-" + image.getOriginalFilename();
    bucketComponent.upload(tempFile, bucketKey);

    var event = SendEmailRequested.builder().bucketKey(bucketKey).to(email).build();
    eventProducer.accept(List.of(event));

    return post;
  }

  public List<Posts> getAllPosts() {
    return postRepository.findAll();
  }
}
