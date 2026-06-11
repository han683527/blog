package org.example.blog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.entity.Comment;
import org.example.blog.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/comment")
public class CommentController {

    private CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    @PostMapping
    public String createComment(@Valid @RequestBody CommentRequest commentRequest, HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.createComment(userId,commentRequest.getArticleId(),commentRequest.getContent());
        return "评论成功";
    }

    @GetMapping("/article/{articleId}")
    public Object getCommentByArticleId(@PathVariable Long articleId){
        List<Comment> comment = commentService.getCommentByArticleId(articleId);
        if(comment.isEmpty()){
            return "暂无评论";
        }
        return comment;
    }
}
