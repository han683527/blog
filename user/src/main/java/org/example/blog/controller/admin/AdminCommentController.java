package org.example.blog.controller.admin;


import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Comment;
import org.example.blog.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin/comment")
@RestController
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{id}")
    public Result<String> adminDeleteCommentById(@PathVariable Long id){
        commentService.adminDeleteCommentById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<CommentResponse>> pageComment(@RequestBody CommentSearchRequest request){
        return Result.success(commentService.pageComment(request));
    }
}
