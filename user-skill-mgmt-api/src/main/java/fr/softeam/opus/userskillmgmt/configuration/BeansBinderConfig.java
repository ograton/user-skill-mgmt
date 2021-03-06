package fr.softeam.opus.userskillmgmt.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import fr.softeam.opus.userskillmgmt.business.hello.HelloBlo;
import fr.softeam.opus.userskillmgmt.business.version.VersionBlo;
import fr.softeam.opus.userskillmgmt.services.HelloService;
import fr.softeam.opus.userskillmgmt.services.VersionService;

public class BeansBinderConfig extends AbstractModule {

    @Provides
    @Singleton
    public HelloService provideHelloService() {
        return new HelloBlo();
    }

    @Provides
    @Singleton
    public VersionService provideHelloBlo() {
        return new VersionBlo();
    }

    @Override
    protected void configure() {

    }
}
