package org.example.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.CommentSearchRequest;
import org.example.blog.dto.request.PageRequest;
import org.example.blog.dto.response.CommentResponse;
import org.example.blog.dto.response.PageResponse;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Comment;
import org.example.blog.service.CommentService;
import org.example.blog.util.UserContext;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<String> createCommentByArticleId(@Valid @RequestBody CommentRequest request){
        commentService.createCommentByArticleId(request);
        return Result.success("评论成功");
    }

    @PostMapping("/list")
    public Result<PageResponse<CommentResponse>> pageComment(@RequestBody CommentSearchRequest request) {
        return Result.success(commentService.pageComment(request));
    }

    @GetMapping("/{id}")
    public Result<Comment> getCommentById(@PathVariable Long id){
        return Result.success(commentService.getCommentById(id));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCommentById(@PathVariable Long id){
        commentService.deleteCommentById(id);
        return Result.success("删除成功");
    }

    @PutMapping
    public Result<String> updateCommentById(@Valid @RequestBody CommentRequest request){
        commentService.updateCommentById(request);
        return Result.success("修改成功");
    }

//    // 查询文章下的评论
//    @GetMapping("/{id}/comments")
//    public Result<PageResponse<CommentResponse>> pageCommentByArticleId(@PathVariable Long id,
//                                                                        @RequestParam(defaultValue = "1") int page,
//                                                                        @RequestParam(defaultValue = "10") int size) {
//        return Result.success(commentService.pageCommentByArticleId(id, page, size));
//    }
}
