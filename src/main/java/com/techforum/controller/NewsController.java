package com.techforum.controller;

import com.techforum.model.Comment;
import com.techforum.model.Post;
import com.techforum.model.User;
import com.techforum.model.Vote;
import com.techforum.repository.CommentRepository;
import com.techforum.repository.PostRepository;
import com.techforum.repository.UserRepository;
import com.techforum.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class NewsController {

   @Autowired
   PostRepository postRepository;

   @Autowired
   VoteRepository voteRepository;

   @Autowired
   UserRepository userRepository;

   @Autowired
   CommentRepository commentRepository;
   
   @PostMapping("/users/login")
   public String login(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

      if ((user.getPassword() == null || user.getPassword().isEmpty())
            || (user.getEmail() == null || user.getEmail().isEmpty())) {
         model.addAttribute("notice", "Имейл адресът и паролата трябва да бъдат попълнени, за да влезете!");
         return "login";
      }

      User sessionUser = userRepository.findUserByEmail(user.getEmail());

      try {
         if (sessionUser == null) {
            model.addAttribute("notice", "Имейл адресът не е разпознат!");
            return "login";
         }
      } catch (NullPointerException e) {
         model.addAttribute("notice", "Имейл адресът не е разпознат!");
         return "login";
      }

      String sessionUserPassword = sessionUser.getPassword();
      boolean isPasswordValid = BCrypt.checkpw(user.getPassword(), sessionUserPassword);
      if (!isPasswordValid) {
         model.addAttribute("notice", "Паролата не е валидна!");
         return "login";
      }

      sessionUser.setLoggedIn(true);
      request.getSession().setAttribute("SESSION_USER", sessionUser);


      return "redirect:/";
   }
   
   @PostMapping("/users")
   public String signup(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

      if ((user.getUsername() == null || user.getUsername().isEmpty())
            || (user.getPassword() == null || user.getPassword().isEmpty())
            || (user.getEmail() == null || user.getPassword().isEmpty())) {
         model.addAttribute("notice", "За да се регистрирате потребителско име, имейл адрес и парола трябва да бъдат попълнени!");
         return "login";
      }

      try {
         user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
         userRepository.save(user);
      } catch (DataIntegrityViolationException e) {
         model.addAttribute("notice",
               "Имейл адресът не е наличен! Моля, изберете различен уникален имейл адрес.");
         return "login";
      }

      User sessionUser = userRepository.findUserByEmail(user.getEmail());
         
      if (sessionUser == null) {
         model.addAttribute("notice", "Потребителят не е разпознат!");
         return "login";
      }

      sessionUser.setLoggedIn(true);
      request.getSession().setAttribute("SESSION_USER", sessionUser);

      return "redirect:/";
   }
   
   @PostMapping("/posts")
   public String addPostDashboardPage(@ModelAttribute Post post, Model model, HttpServletRequest request) {

      if ((post.getTitle() == null || post.getTitle().isEmpty())
            || (post.getContent() == null || post.getContent().isEmpty())) {
         return "redirect:/dashboardEmptyTitleAndLink";
      }

      if (request.getSession(false) == null) {
         return "redirect:/login";
      } else {
         User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
         post.setUserId(sessionUser.getId());
         postRepository.save(post);

         return "redirect:/";
      }
   }
      
   @PostMapping("/posts/{id}")
   public String updatePostDashboardPage(@PathVariable int id, @ModelAttribute Post post, Model model,
         HttpServletRequest request) {

      if (request.getSession(false) == null) {
         model.addAttribute("user", new User());
         return "redirect/";
      } else {
         Post tempPost = postRepository.getById(id);
         tempPost.setTitle(post.getTitle());
         postRepository.save(tempPost);

         return "redirect:/";
      }
   }
   
   @PostMapping("/comments")
   public String createCommentCommentsPage(@ModelAttribute Comment comment, Model model, HttpServletRequest request) {

      if (comment.getCommentText() == null || comment.getCommentText().isEmpty()) {
         return "redirect:/singlePostEmptyComment/" + comment.getPostId();
      } else {
         if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            comment.setUserId(sessionUser.getId());
            commentRepository.save(comment);
            return "redirect:/post/" + comment.getPostId();
         } else {
            return "login";
         }
      }
   }
   
   @PostMapping("/comments/edit")
   public String createCommentEditPage(@ModelAttribute Comment comment, HttpServletRequest request) {

      if (comment.getCommentText() == null || comment.getCommentText().isEmpty()) {
         return "redirect:/editPostEmptyComment/" + comment.getPostId();
      } else {
         if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            comment.setUserId(sessionUser.getId());
            commentRepository.save(comment);

            return "redirect:/dashboard/edit/" + comment.getPostId();
         } else {
            return "redirect:/login";
         }
      }
   }
   
   @PutMapping("/posts/upvote")
   public void addVoteCommentsPage(@RequestBody Vote vote, HttpServletRequest request, HttpServletResponse response) {

      if (request.getSession(false) != null) {
         Post returnPost = null;
         User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
         vote.setUserId(sessionUser.getId());
         voteRepository.save(vote);

         returnPost = postRepository.getById(vote.getPostId());
         returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));
      }
   }
}