package com.c4_soft.springaddons.rest.reactive;

import java.util.Optional;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import com.c4_soft.springaddons.rest.SpringAddonsRestProperties;
import com.c4_soft.springaddons.rest.SystemProxyProperties;
import lombok.Setter;

@Setter
public class ServerWebClientFactoryBean implements FactoryBean<WebClient> {
  private String clientId;
  private SystemProxyProperties systemProxyProperties;
  private SpringAddonsRestProperties restProperties;
  private Optional<ReactiveClientRegistrationRepository> clientRegistrationRepository =
      Optional.empty();
  private Optional<ServerOAuth2AuthorizedClientRepository> authorizedClientRepository =
      Optional.empty();

  @Override
  @Nullable
  public WebClient getObject() throws Exception {
    final var builderFactoryBean = new ServerWebClientBuilderFactoryBean();
    builderFactoryBean.setClientId(clientId);
    builderFactoryBean.setSystemProxyProperties(systemProxyProperties);
    builderFactoryBean.setRestProperties(restProperties);
    builderFactoryBean.setClientRegistrationRepository(clientRegistrationRepository);
    builderFactoryBean.setAuthorizedClientRepository(authorizedClientRepository);
    return Optional.ofNullable(builderFactoryBean.getObject()).map(WebClient.Builder::build)
        .orElse(null);
  }

  @Override
  @Nullable
  public Class<?> getObjectType() {
    return WebClient.class;
  }
}