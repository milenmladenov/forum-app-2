package com.techforum.service;

import com.techforum.model.Post;
import com.techforum.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    PostRepository postRepository;

    public List<Post> listAll(String content){
        if (content == null) {
            return postRepository.findAll();
        }
        return postRepository.searchByContentLike(content);
    }

}
