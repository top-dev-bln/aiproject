package com.demo.service;

import com.demo.model.User;
import com.demo.payload.response.UserProfile;
import com.demo.repo.UserRepository;
import com.demo.exception.NotFoundException;
import com.demo.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserProfile getUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                .build();
    }

    public UserProfile getCurrentUserProfile() {
        String username = AuthUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                .build();
    }

    public User addUser(com.demo.dto.User user) {
        return userRepository.save(
                User.builder().email(user.getEmail()).username(user.getUsername()).password(user.getPassword()).build()
        );
    }

    /**
     * Updates user product statistics based on product events.
     * This method is called when the account service receives product events from Kafka.
     * 
     * @param userId the ID of the user who performed the product action
     * @param action the action performed (CREATED, UPDATED, DELETED)
     */
    public void updateUserProductStats(Long userId, String action) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User with ID {} not found when updating product stats for action: {}", userId, action);
                return;
            }

            log.info("Updating product stats for user: {} (action: {})", user.getUsername(), action);

            // Here you could:
            // 1. Update user statistics in database
            // 2. Send notifications to the user
            // 3. Update user activity logs
            // 4. Trigger other business logic

            // For now, we'll just log the activity
            switch (action) {
                case "CREATED":
                    log.info("User {} created a new product", user.getUsername());
                    // Could increment a products_created counter
                    break;
                case "UPDATED":
                    log.info("User {} updated a product", user.getUsername());
                    // Could increment a products_updated counter
                    break;
                case "DELETED":
                    log.info("User {} deleted a product", user.getUsername());
                    // Could increment a products_deleted counter
                    break;
                default:
                    log.warn("Unknown product action: {}", action);
            }

        } catch (Exception e) {
            log.error("Error updating product stats for user {} with action {}: {}", 
                     userId, action, e.getMessage(), e);
        }
    }

}
