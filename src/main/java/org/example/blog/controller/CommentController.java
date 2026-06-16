package org.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.blog.dto.request.CommentRequest;
import org.example.blog.dto.request.UpdateCommentRequest;
import org.example.blog.dto.response.Result;
import org.example.blog.entity.Comment;
import org.example.blog.service.CommentService;
import org.example.blog.util.UserContext;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

//    public CommentController(CommentService commentService){
//        this.commentService = commentService;
//    }

    @PostMapping
    public Result<String> createComment(@Valid @RequestBody CommentRequest commentRequest){
        Long userId = UserContext.get();
        commentService.createComment(userId,commentRequest.getArticleId(),commentRequest.getContent());
//        return "评论成功";
        return Result.success("评论成功");
    }

    @GetMapping
    public Result<IPage<Comment>> getAllComment(@RequestParam(defaultValue = ("1")) int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.pageComment(page,size));
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

    @PutMapping("/{id}")
    public Result<String> updateCommentById(@Valid @RequestBody UpdateCommentRequest commentRequest ,
                                    @PathVariable Long id){
        commentService.updateCommentById(id,commentRequest.getContent());
        return Result.success("修改成功");
    }
}
