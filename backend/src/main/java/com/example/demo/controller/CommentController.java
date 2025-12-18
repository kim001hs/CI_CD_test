package com.example.demo.controller;

import com.example.demo.dto.CommentRequestDto;
import com.example.demo.dto.CommentResponseDto;
import com.example.demo.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable Long postId,
                                                            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(commentService.createComment(userDetails.getUsername(), postId, requestDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable Long commentId,
                                                            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(commentService.updateComment(userDetails.getUsername(), commentId, requestDto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long commentId) {
        commentService.deleteComment(userDetails.getUsername(), commentId);
        return ResponseEntity.ok().build();
    }
}
