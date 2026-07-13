package com.school.hei.endpoint.rest.controller;

import com.school.hei.entity.Posts;
import com.school.hei.service.PostsService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostsController {
  private final PostsService postService;

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<Posts> createPost(
      @RequestPart("image") MultipartFile image,
      @RequestParam String name,
      @RequestParam String email) {
    var post = postService.createPost(image, name, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(post);
  }

  @GetMapping()
  public ResponseEntity<List<Posts>> getAllPosts() {
    return ResponseEntity.ok(postService.getAllPosts());
  }
}
