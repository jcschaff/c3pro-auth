package edu.uconn.c3pro.server.auth.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {
//    @Bean
//    public EmbeddedServletContainerCustomizer containerCustomizer() {
//        return (container -> {
//            container.setPort(8081);
//        });
//    }
    
//    @Bean
//    public EmbeddedServletContainerFactory servletContainer() {
//      TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//        @Override
//        protected void postProcessContext(Context context) {
//          SecurityConstraint securityConstraint = new SecurityConstraint();
//          securityConstraint.setUserConstraint("CONFIDENTIAL");
//          SecurityCollection collection = new SecurityCollection();
//          collection.addPattern("/*");
//          securityConstraint.addCollection(collection);
//          context.addConstraint(securityConstraint);
//        }
//      };
//      tomcat.addAdditionalTomcatConnectors(getHttpConnector());
//      return tomcat;
//    }
//    private Connector getHttpConnector() {
//      Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//      connector.setScheme("http");
//      connector.setPort(8080);
//      connector.setSecure(false);
//      connector.setRedirectPort(8443);
//      return connector;
//    }

}