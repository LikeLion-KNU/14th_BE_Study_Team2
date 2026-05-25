package com.example.community.domain.post.controller;

import com.example.community.domain.post.dto.CommentRequest;
import com.example.community.domain.post.dto.CommentResponse;
import com.example.community.domain.post.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Long> create(@PathVariable Long postId, @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.create(postId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAll(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findAllByPost(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(@PathVariable Long commentId, @RequestBody CommentRequest request) {
        commentService.update(commentId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}