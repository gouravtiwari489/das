package com.datagenerator.demo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import com.datagenerator.demo.utils.CustomTokenConverter;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
    private AuthenticationManager authenticationManager;
	
	@Autowired
	private TokenStore tokenStore;
	
	
	@Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory() 
	        .withClient("client") 
	        .secret("clientpassword")
	        .scopes("read", "write") 
	        .authorizedGrantTypes("password","refresh_token","authorization_code")
	        .accessTokenValiditySeconds(50000);
    }
	
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

	    endpoints.tokenStore(tokenStore())
	            .tokenEnhancer(customTokenEnhancer())
	            .authenticationManager(authenticationManager);
	}

	@Bean 
	public CustomTokenConverter customTokenEnhancer() {
	    return new CustomTokenConverter();
	}
	
	@Bean
	public TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}


}