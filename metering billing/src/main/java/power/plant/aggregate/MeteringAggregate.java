package power.plant.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.*;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;
import power.plant.command.*;
import power.plant.event.*;
import power.plant.query.*;

@Aggregate
@Data
@ToString
public class MeteringAggregate {

    @AggregateIdentifier
    private String id;

    private String yearCode;
    private String monthCode;
    private String dayCode;
    private String subscriberId;
    private String platId;
    private Double generationAmount;
    private Double sep;

    public MeteringAggregate() {}

    @CommandHandler
    public void handle(CalculateCommand command) {
        CalculatedEvent event = new CalculatedEvent();
        BeanUtils.copyProperties(command, event);

        apply(event);
    }

    @CommandHandler
    public MeteringAggregate(CreateMeterCommand command) {
        MeterCreatedEvent event = new MeterCreatedEvent();
        BeanUtils.copyProperties(command, event);

        //TODO: check key generation is properly done
        if (event.getId() == null) event.setId(createUUID());

        apply(event);
    }

    private String createUUID() {
        return UUID.randomUUID().toString();
    }

    @EventSourcingHandler
    public void on(CalculatedEvent event) {
        //TODO: business logic here

    }

    @EventSourcingHandler
    public void on(MeterCreatedEvent event) {
        BeanUtils.copyProperties(event, this);
        //TODO: business logic here

    }
}
