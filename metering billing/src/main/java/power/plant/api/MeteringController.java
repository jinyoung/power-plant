package power.plant.api;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import power.plant.aggregate.*;
import power.plant.command.*;

@RestController
public class MeteringController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public MeteringController(
        CommandGateway commandGateway,
        QueryGateway queryGateway
    ) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @RequestMapping(
        value = "/meterings/{id}/calculate",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public CompletableFuture calculate(
        @PathVariable("id") String id,
        @RequestBody CalculateCommand calculateCommand
    ) throws Exception {
        System.out.println("##### /metering/calculate  called #####");

        calculateCommand.setId(id);
        // send command
        return commandGateway.send(calculateCommand);
    }

    @RequestMapping(value = "/meterings", method = RequestMethod.POST)
    public CompletableFuture createMeter(
        @RequestBody CreateMeterCommand createMeterCommand
    ) throws Exception {
        System.out.println("##### /metering/createMeter  called #####");

        // send command
        return commandGateway
            .send(createMeterCommand)
            .thenApply(id -> {
                MeteringAggregate resource = new MeteringAggregate();
                BeanUtils.copyProperties(createMeterCommand, resource);

                resource.setId((String) id);

                return new ResponseEntity<>(hateoas(resource), HttpStatus.OK);
            });
    }

    @Autowired
    EventStore eventStore;

    @GetMapping(value = "/meterings/{id}/events")
    public ResponseEntity getEvents(@PathVariable("id") String id) {
        ArrayList resources = new ArrayList<MeteringAggregate>();
        eventStore.readEvents(id).asStream().forEach(resources::add);

        CollectionModel<MeteringAggregate> model = CollectionModel.of(
            resources
        );

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    EntityModel<MeteringAggregate> hateoas(MeteringAggregate resource) {
        EntityModel<MeteringAggregate> model = EntityModel.of(resource);

        model.add(Link.of("/meterings/" + resource.getId()).withSelfRel());

        model.add(
            Link
                .of("/meterings/" + resource.getId() + "/calculate")
                .withRel("calculate")
        );

        model.add(
            Link
                .of("/meterings/" + resource.getId() + "/events")
                .withRel("events")
        );

        return model;
    }
}
