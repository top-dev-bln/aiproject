package com.arch.demo;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        User saved = userRepository.save(user);
        emailService.sendWelcome(saved.getEmail());
        return saved;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
