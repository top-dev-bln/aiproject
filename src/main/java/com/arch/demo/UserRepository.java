package com.arch.demo;

import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository {

    User findById(Long id);

    List<User> findAll();

    User save(User user);

    void deleteById(Long id);
}
