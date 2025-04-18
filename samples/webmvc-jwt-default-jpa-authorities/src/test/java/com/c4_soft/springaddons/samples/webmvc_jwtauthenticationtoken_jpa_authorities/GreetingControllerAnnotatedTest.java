/*
 * Copyright 2019 Jérôme Wacongne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.c4_soft.springaddons.samples.webmvc_jwtauthenticationtoken_jpa_authorities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.annotations.parameterized.ParameterizedAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties;
import jakarta.persistence.EntityManagerFactory;

/**
 * @author Jérôme Wacongne &lt;ch4mp&#64;c4-soft.com&gt;
 */
@WebMvcTest(GreetingController.class)
@Import({ TestUserAuthorityRepositoryConf.class, SecurityConfig.class })
@AutoConfigureAddonsWebmvcResourceServerSecurity
class GreetingControllerAnnotatedTest {

	@MockitoBean
	private MessageService messageService;

	@Autowired
	MockMvcSupport api;

	@Autowired
	WithJwt.AuthenticationFactory authFactory;

	@Autowired
	SpringAddonsOidcProperties springAddonsOidcProperties;

	@MockitoBean(name = "entityManagerFactory")
	EntityManagerFactory entityManagerFactory;

	@BeforeEach
	public void setUp() {
		when(messageService.greet(any())).thenAnswer(invocation -> {
			final JwtAuthenticationToken auth = invocation.getArgument(0, JwtAuthenticationToken.class);
			return String.format("Hello %s! You are granted with %s.", auth.getName(), auth.getAuthorities());
		});
		when(messageService.getSecret()).thenReturn("Secret message");
	}

	@Test
	void givenRequestIsAnonymous_whenGetGreet_thenUnauthorized() throws Exception {
		api.get("/greet").andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void givenUserHasMockedAuthenticated_whenGetGreet_thenOk() throws Exception {
		api.get("/greet").andExpect(content().string("Hello user! You are granted with [ROLE_AUTHORIZED_PERSONNEL]."));
	}

	@ParameterizedTest
	@MethodSource("identities")
	void givenUserIsAuthenticated_whenGetGreet_thenOk(@ParameterizedAuthentication Authentication auth) throws Exception {
		api.get("/greet").andExpect(content().string("Hello %s! You are granted with %s.".formatted(auth.getName(), auth.getAuthorities())));
	}

	Stream<AbstractAuthenticationToken> identities() {
		return authFactory.authenticationsFrom("ch4mp.json", "tonton-pirate.json");
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class, name = "Ch4mpy", authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void givenUserIsMockedAsCh4mpy_whenGetGreet_thenOk() throws Exception {
		api.get("/greet").andExpect(content().string("Hello Ch4mpy! You are granted with [ROLE_AUTHORIZED_PERSONNEL]."));
	}

	@Test
	@WithJwt("ch4mp.json")
	void givenUserIsCh4mpy_whenGetGreet_thenOk() throws Exception {
		api.get("/greet").andExpect(content().string("Hello ch4mp! You are granted with [ROLE_AUTHORIZED_PERSONNEL]."));
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class)
	void givenUserIsNotGrantedWithAuthorizedPersonnel_whenGetSecuredRoute_thenForbidden() throws Exception {
		api.get("/secured-route").andExpect(status().isForbidden());
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void givenUserIsGrantedWithAuthorizedPersonnel_whenGetSecuredRoute_thenOk() throws Exception {
		api.get("/secured-route").andExpect(status().isOk());
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class)
	void givenUserIsNotGrantedWithAuthorizedPersonnel_whenGetSecuredMethod_thenForbidden() throws Exception {
		api.get("/secured-method").andExpect(status().isForbidden());
	}

	@Test
	@WithMockAuthentication(authType = JwtAuthenticationToken.class, principalType = Jwt.class, authorities = "ROLE_AUTHORIZED_PERSONNEL")
	void givenUserIsGrantedWithAuthorizedPersonnel_whenGetSecuredMethod_thenOk() throws Exception {
		api.get("/secured-method").andExpect(status().isOk());
	}
}
