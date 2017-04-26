package com.tndavidson.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *  endpoint that triggers our custom bus event
 *
 * @author Tim Davidson
 */
@RestController
public class PublishEndpoint {

    private ApplicationContext context;

    @Autowired
    public PublishEndpoint(ApplicationContext context) {
        this.context = context;
    }

    @RequestMapping(value="/publish",method= RequestMethod.POST)
    public String publish() {
        final String myUniqueId = context.getId(); // each service instance must have a unique context ID

        final MyCustomRemoteEvent event =
                new MyCustomRemoteEvent(this, myUniqueId, "hello world");

        context.publishEvent(event);

        return "event published";
    }
}
