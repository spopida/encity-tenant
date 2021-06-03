package uk.co.encity.tenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import reactor.util.Logger;
import reactor.util.Loggers;
import uk.co.encity.tenancy.commands.PatchTenancyCommand;
import uk.co.encity.tenancy.commands.PreConditionException;
import uk.co.encity.tenancy.commands.TenancyCommand;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.io.IOException;

@Service
@Getter
public class TenancyService {

    /**
     * The {@link Logger} for this class
     */
    private final Logger logger = Loggers.getLogger(getClass());

    /**
     * The repository of users
     */
    private final TenancyRepository repository;

    /**
     * The RabbitMQ helper class
     */
    private final AmqpTemplate amqpTemplate;

    /**
     * The name of the AMQP exchange used for message publication
     */
    private final String topicExchangeName = "encity-exchange";

    private final ObjectMapper mapper;

    public TenancyService(TenancyRepository repo, AmqpTemplate rabbitTmpl, ObjectMapper mapper) {
        this.repository = repo;
        this.amqpTemplate = rabbitTmpl;
        this.mapper = mapper;
    }

    /**
     * Attempt to perform a state transition command on a Tenancy, applying the necessary
     * business logic and saving the event
     * @param command the command (transition) to perform
     * @return the affected Tenancy
     */
    // TODO: distinguish between tenancy-initiated transitions/states and provider-initiated ones
    public Tenancy applyCommand(PatchTenancyCommand command, SimpleModule module, ObjectMapper mapper) throws
    //public Tenancy applyCommand(PatchTenancyCommand command) throws
            UnsupportedOperationException,
            IllegalArgumentException,
            PreConditionException,
            IOException
    {
        TenancyRepository tenancyRepo = this.getRepository();

        // Try to get the tenancy referenced in the command (could throw IOException)
        Tenancy theTenancy = tenancyRepo.getTenancy(command.getHexTenancyId());

        if (theTenancy != null) {
            // Check the pre-conditions of the command - might throw
            command.checkPreConditions(theTenancy);
        } else {
            throw new IllegalArgumentException(String.format("Tenancy with id %s does not exist", command.getHexTenancyId()), null);
        }

        // Perform the command
        switch (command.getCmdType()) {
            case CONFIRM_TENANCY:
            case REJECT_TENANCY:
            case CHANGE_PORTFOLIO:
                // No special actions (yet!)- the event will be saved (below)
                break;
            default:
                throw new UnsupportedOperationException("Command not Supported: " + command.getCmdType().toString(), null);
        }

        // Save an event
        TenancyEvent evt = command.createTenancyEvent(theTenancy);
        this.repository.captureEvent(evt.getEventType(), evt);

        // Publish the event
        //SimpleModule module = new SimpleModule();
        this.getLogger().debug("Sending message...");
        evt.addSerializerToModule(module);
        mapper.registerModule(module);
        //this.mapper.registerModule(module);

        String jsonEvt;

        jsonEvt = mapper.writeValueAsString(evt);
        //jsonEvt = this.getMapper().writeValueAsString(evt);
        this.getAmqpTemplate().convertAndSend(this.getTopicExchangeName(), evt.getRoutingKey(), jsonEvt);

        return theTenancy;
    }
}

