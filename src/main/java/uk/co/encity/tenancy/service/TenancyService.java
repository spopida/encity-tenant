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

        // TODO: Supporting REQUEST_HMRC_VAT_DIRECT_AUTHORISATION....
        //
        // - add it to the case statement below
        // - when applying the event to the tenancy I'll have to build some retrieval logic... the event may have expired for example
        // - update the tenancy with attributes relating to the LAST authorisation request only
        // - meantime, publish the event
        // - let the notification service pick up the event and email the contact
        // - allow the contact to click on the link and visit a non-authenticated page to confirm authorisation
        // - go to the HMRC site with a re-direct back to govbuddy - another non-authenticated page that accepts the token and stores it
        // - this token handling will be done in the HMRC microservice
        // - then we'll include retrieval of tokens as part of the hydration of a tenancy
        // - and the dashboard should know whether it can retrieve VAT
        // - when all that is working, go back and scrap the agency stuff - all authorisation will be direct


        // Perform the command
        switch (command.getCmdType()) {
            case CONFIRM_TENANCY:
            case REJECT_TENANCY:
            case CHANGE_PORTFOLIO:
            case CHANGE_HMRC_AGENT_VAT_ENABLEMENT:
            case REQUEST_HMRC_AGENT_VAT_AUTHORISATION:
            case CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT:
            case CHANGE_PORTFOLIO_MEMBER_DIRECT_AUTH:
            case CHANGE_PORTFOLIO_MEMBER_VAT_REG_NO:
            case CHANGE_PORTFOLIO_MEMBER_DIRECT_CONTACT:
            case ADD_PORTFOLIO_MEMBER:
            case DELETE_PORTFOLIO_MEMBER:
            case REQUEST_HMRC_VAT_AUTHZ:
            case REJECT_HMRC_VAT_AUTHZ:
                // No special actions (yet!)- the event will be saved (below)
                break;
            default:
                throw new UnsupportedOperationException("Command not supported: " + command.getCmdType().toString(), null);
        }

        // Save an event
        TenancyEvent evt = command.createTenancyEvent(theTenancy);
        this.repository.captureEvent(evt.getEventType(), evt);

        // Publish the event
        this.getLogger().debug("Sending message...");
        evt.addSerializerToModule(module);
        mapper.registerModule(module);

        String jsonEvt;

        jsonEvt = mapper.writeValueAsString(evt);
        this.getAmqpTemplate().convertAndSend(this.getTopicExchangeName(), evt.getRoutingKey(), jsonEvt);

        return theTenancy;
    }
}

