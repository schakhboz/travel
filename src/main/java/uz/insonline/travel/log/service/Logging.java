package uz.insonline.travel.log.service;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Logging {
    private static final Logger log = LoggerFactory.getLogger(Logging.class.getName());

    @Override
    public String toString(){
        return "logging";
    }

    public void log(String msg){
        log.info(msg);
    }
}
