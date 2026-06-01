package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    private String username;

    private String email;

    private String password;

}
