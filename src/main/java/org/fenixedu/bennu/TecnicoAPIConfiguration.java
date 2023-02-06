package org.fenixedu.bennu;

import pt.ist.tecnicoapi.serializer.TecnicoAPISerializer;
import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;

@BennuSpringModule(basePackages = "pt.ist.tecnicoapi.ui", bundles = "FenixEduAPIResources")
public class TecnicoAPIConfiguration {
    public final static String BUNDLE = "resources.TecnicoAPIResources";
    private final static TecnicoAPISerializer serializer = new TecnicoAPISerializer();

    @ConfigurationManager(description = "Tecnico API Configuration")
    public interface ConfigurationProperties {
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

    public static TecnicoAPISerializer getSerializer() {
        return serializer;
    }

}
