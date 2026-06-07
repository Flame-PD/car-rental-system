package com.example.carrental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 静态资源
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 客户端所有页面和请求（不需要登录）
                        .requestMatchers("/cars/**", "/search/**", "/rent/**", "/return/**",
                                "/user/**", "/login", "/register").permitAll()
                        // 管理端需要登录
                        .requestMatchers("/admin/**").authenticated()
                        // 其他请求
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/cars", true)  // 登录成功后跳转到客户端首页
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/cars")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());  // 临时关闭CSRF，方便测试

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 创建管理员账号
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
