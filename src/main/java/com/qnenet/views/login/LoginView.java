package com.qnenet.views.login;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("login")
public class LoginView extends VerticalLayout {
    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        Button loginButton = new Button("Login");
        loginButton.addClickListener(event -> {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username.getValue(), password.getValue());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            VaadinSession.getCurrent().setAttribute(
                    SecurityContext.class, SecurityContextHolder.getContext());
            UI.getCurrent().navigate("");
        });
        add(username, password, loginButton);
    }
}


// To create a login view using Vaadin and Spring Security, you can follow these steps:

// 1. Add the necessary dependencies to your project's build file (e.g., `pom.xml` for Maven):

// ```xml
// <dependency>
//     <groupId>com.vaadin</groupId>
//     <artifactId>vaadin-spring-boot-starter</artifactId>
// </dependency>
// <dependency>
//     <groupId>org.springframework.boot</groupId>
//     <artifactId>spring-boot-starter-security</artifactId>
</dependency>
// ```

// 2. Create a Vaadin view for the login page:

// ```java
// @Route("login")
// public class LoginView extends VerticalLayout {

//     public LoginView() {
//         setSizeFull();
//         setAlignItems(Alignment.CENTER);
//         setJustifyContentMode(JustifyContentMode.CENTER);

//         TextField username = new TextField("Username");
//         PasswordField password = new PasswordField("Password");
//         Button loginButton = new Button("Login");

//         loginButton.addClickListener(event -> {
//             Authentication authentication = new UsernamePasswordAuthenticationToken(
//                     username.getValue(), password.getValue());
//             SecurityContextHolder.getContext().setAuthentication(authentication);
//             VaadinSession.getCurrent().setAttribute(
//                     SecurityContext.class, SecurityContextHolder.getContext());
//             UI.getCurrent().navigate("");
//         });

//         add(username, password, loginButton);
//     }
// }
// ```

// 3. Configure Spring Security:

// ```java
// @Configuration
// @EnableWebSecurity
// public class SecurityConfig extends VaadinWebSecurityConfigurerAdapter {

//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         super.configure(http);
//         setLoginView(http, LoginView.class);
//     }

//     @Override
//     public void configure(WebSecurity web) throws Exception {
//         super.configure(web);
//         web.ignoring().antMatchers("/images/**");
//     }
// }
// ```

// 4. Secure your Vaadin views using `@Secured` or `@PreAuthorize` annotations:

// ```java
// @Route("")
// @Secured("ROLE_USER")
// public class MainView extends VerticalLayout {
//     // ...
// }
// ```

// 5. Implement the `UserDetailsService` to load user details:

// ```java
// @Service
// public class UserDetailsServiceImpl implements UserDetailsService {

//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         // Load user details from your data source (e.g., database)
//         // Return an instance of org.springframework.security.core.userdetails.User
//         // with the user's username, password, and authorities
//     }
// }
// ```

// 6. Run your Vaadin application.

// When you access the application, you will be redirected to the login view. After entering valid credentials, you will be authenticated and redirected to the main view.

// Note: Make sure to replace the placeholder code in the `UserDetailsServiceImpl` with your actual implementation to load user details from your data source.

// This is a basic setup for integrating Vaadin with Spring Security for authentication. You can further customize the login view, add error handling, and implement additional security features as needed.

