
package com.example.registerapp.service;

import com.example.registerapp.dto.LoginDto;
import com.example.registerapp.dto.RegisterDto;
import com.example.registerapp.entity.User;
import com.example.registerapp.repository.UserRepository;
import com.example.registerapp.util.EmailUtil;
import com.example.registerapp.util.OtpUtil;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private OtpUtil otpUtil;
  @Autowired
  private EmailUtil emailUtil;
  @Autowired
  private UserRepository userRepository;

  public String register(RegisterDto registerDto) throws MessagingException {
    String otp = otpUtil.generateOtp();
    emailUtil.sendOtpEmail(registerDto.getEmail(), otp);
    User user = new User();
    user.setName(registerDto.getName());
    user.setEmail(registerDto.getEmail());
    user.setPassword(registerDto.getPassword());
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "User registration successful";
  }

  public String verifyAccount(String email, String otp) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
        LocalDateTime.now()).getSeconds() < (1 * 60)) {
      user.setActive(true);
      userRepository.save(user);
      return "OTP verified you can login";
    }
    return "Please regenerate otp and try again";
  }

  public String regenerateOtp(String email) throws MessagingException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    emailUtil.sendOtpEmail(email, otp);
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 1 minute";
  }

  public String login(LoginDto loginDto) {
    User user = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(
            () -> new RuntimeException("User not found with this email: " + loginDto.getEmail()));
    if (!loginDto.getPassword().equals(user.getPassword())) {
      return "Password is incorrect";
    } else if (!user.isActive()) {
      return "your account is not verified";
    }
    return "Login successful";
  }

public String forgotPassword(String email) {
	User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
	try {
	      emailUtil.sendSetPasswordEmail(email);
	    } catch (MessagingException e) {
	      throw new RuntimeException("Unable to send set password email please try again");
	    }
	return "please check your email to set new password to your account";
}
}
