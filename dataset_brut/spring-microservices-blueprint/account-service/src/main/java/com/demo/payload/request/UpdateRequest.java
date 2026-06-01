package com.demo.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {

  @NotBlank(message = "Email is required")
  @Size(max = 50, message = "Email must not exceed 50 characters")
  @Email(message = "Email must be valid")
  private String email;

}
