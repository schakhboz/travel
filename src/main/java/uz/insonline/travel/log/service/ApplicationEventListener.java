package uz.insonline.travel.log.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener implements ApplicationListener<ApplicationStartedEvent> {
    private final Logging logging;

    @Autowired
    public ApplicationEventListener(Logging logging) {
        this.logging = logging;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        logging.log("Application started");
    }

    @PreDestroy
    public void onShutdown() {
        logging.log("Application stopped");
    }
}
