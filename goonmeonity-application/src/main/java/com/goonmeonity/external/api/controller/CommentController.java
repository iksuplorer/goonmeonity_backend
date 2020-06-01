package com.goonmeonity.external.api.controller;

import com.goonmeonity.domain.entity.user.User;
import com.goonmeonity.external.api.response.comment.CreateCommentResponse;
import com.goonmeonity.external.api.service.AuthService;
import com.goonmeonity.external.api.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/boards/{boardId}/comments")
public class CommentController {
    private CommentService commentService;
    private AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }

    @ApiOperation("댓글 작성")
    @PostMapping("/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCommentResponse createComment(
            @RequestHeader String accessToken,
            @PathVariable long boardId,
            @RequestBody String content
    ){
        User user = authService.getUserByAccessToken(accessToken);

        return commentService.createComment(user, boardId, content);
    }
}