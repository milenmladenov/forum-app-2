package com.techforum.controller;

import com.techforum.model.Post;
import com.techforum.model.User;
import com.techforum.model.Vote;
import com.techforum.repository.PostRepository;
import com.techforum.repository.UserRepository;
import com.techforum.repository.VoteRepository;
import com.techforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class PostController {

   @Autowired
   PostRepository repository;

   @Autowired
   VoteRepository voteRepository;

   @Autowired
   UserRepository userRepository;
   @Autowired
   PostService postService;




   @GetMapping("/api/posts")
   public List<Post> getAllPosts() {
      List<Post> postList = repository.findAll();
      for (Post p : postList) {
         p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
      }
      return postList;
   }


   @GetMapping("/api/posts/{id}")
   public Post getPost(@PathVariable Integer id) {
      Post returnPost = repository.getById(id);
      returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));

      return returnPost;
   }


   @PostMapping("/api/posts")
   @ResponseStatus(HttpStatus.CREATED)
   public Post addPost(@RequestBody Post post) {
      repository.save(post);
      return post;
   }


   @PutMapping("/api/posts/{id}")
   public Post updatePost(@PathVariable int id, @RequestBody Post post) {
      Post tempPost = repository.getById(id);
      tempPost.setTitle(post.getTitle());
      tempPost.setContent(post.getContent());
      return repository.save(tempPost);
   }


   @PutMapping("/api/posts/upvote")
   public String addVote(@RequestBody Vote vote, HttpServletRequest request) {
      String returnValue = "";

      if(request.getSession(false) != null) {
         Post returnPost = null;

         User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
         vote.setUserId(sessionUser.getId());
         voteRepository.save(vote);

         returnPost = repository.getById(vote.getPostId());
         returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));

         returnValue = "";
      } else {
         returnValue = "login";
      }

      return returnValue;
   }


   @DeleteMapping("/api/posts/{id}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void deletePost(@PathVariable int id) {
      repository.deleteById(id);
   }
}