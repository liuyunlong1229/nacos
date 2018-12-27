package com.alibaba.nacos;

import com.alibaba.nacos.console.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Spring security config
 *
 * @author Nacos
 */
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String AUTHORIZATION_TOKEN = "access_token";

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            //自定义获取用户信息
            .userDetailsService(userDetailsService)
            //设置密码加密
            .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // TODO 做开关是否开启登录功能
        if (false) {
            http.authorizeRequests().antMatchers("/").permitAll();
        } else {
            http
                // since we use jwt, csrf is not necessary
                .csrf().disable()
                // since we use jwt, session is not necessary
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                // TODO: we should use a better way to match the resources
                // requests for resource and auth api are always allowed
                .antMatchers("/", "/*.html", "/**/*.js", "/**/*.css", "/favicon.ico", "/**/*.html", "/**/*.svg", "/console-fe/public/*", "/**/*.png", "/*.png").permitAll()
                .antMatchers("/v1/cs/health").permitAll()
                .antMatchers("/v1/auth/**").permitAll()
                .anyRequest().authenticated();
            http.addFilterAfter(genericFilterBean(), UsernamePasswordAuthenticationFilter.class);

            // disable cache
            http.headers().cacheControl();
        }
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public GenericFilterBean genericFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }

}
