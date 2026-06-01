package com.demo.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
public class LoginRequest {

	@NotBlank(message = "Username is required")
  	private String username;

	@NotBlank(message = "Password is required")
	@ToString.Exclude
	private String password;

}
