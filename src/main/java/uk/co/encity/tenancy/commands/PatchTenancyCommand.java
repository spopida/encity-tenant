package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.entity.VatSettings;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Getter
public abstract class PatchTenancyCommand extends TenancyCommand {

    // TODO: This command type should be in the base class!
    private TenancyCommand.TenancyTenantCommandType cmdType;
    private String hexTenancyId;

    public PatchTenancyCommand(TenancyCommand.TenancyTenantCommandType cmdType, String hexTenancyId) {
        this.hexTenancyId = hexTenancyId;
        this.cmdType = cmdType;
    }

    public TenancyTenantCommandType getCommandType() { return this.cmdType; }
    public String getHexTenancyId() { return this.hexTenancyId; }

    public abstract void checkPreConditions(Tenancy t) throws PreConditionException;
    public abstract TenancyEvent createTenancyEvent(Tenancy t);

    /**
     * Get a command instance for portfolio-level commands
     * @param cmdtype
     * @param hexTenancyId
     * @param companyId
     * @param node
     * @return the command
     * @throws UnsupportedOperationException if an unexpected command type is passed
     * @throws JsonProcessingException if there is an error processing the JSON node
     */
    public static PatchTenancyCommand getPatchTenancyCommand(
            @NonNull TenancyCommand.TenancyTenantCommandType cmdtype,
            String hexTenancyId,
            String companyId,
            JsonNode node) throws UnsupportedOperationException, JsonProcessingException {
        PatchTenancyCommand patchCmd = null;

        //JsonNode value = null;
        JsonNode value = node.get("value");

        String vatRegNo = null;
        boolean directAuthorisation = false;
        String directContactEmail = null;

        switch (cmdtype) {
            case CHANGE_PORTFOLIO_MEMBER_VAT_ENABLEMENT:
                //value = node.get("value");
                boolean enabled = value.get("vatEnabled").booleanValue();
                patchCmd = new ChangePortfolioMemberVatEnablementCommand(hexTenancyId, companyId, enabled);
                break;
            case CHANGE_PORTFOLIO_MEMBER_VAT_REG_NO:
                //value = node.get("value");
                vatRegNo = value.get("vatRegNo").asText();
                patchCmd = new ChangePortfolioMemberVatRegNoCommand(hexTenancyId, companyId, vatRegNo);
                break;
            case CHANGE_PORTFOLIO_MEMBER_DIRECT_AUTH:
                //value = node.get("value");
                directAuthorisation = value.get("directAuthorisation").booleanValue();
                patchCmd = new ChangePortfolioMemberDirectAuthCommand(hexTenancyId, companyId, directAuthorisation);
                break;
            case CHANGE_PORTFOLIO_MEMBER_DIRECT_CONTACT:
                //value = node.get("value");
                directContactEmail = value.get("directContactEmail").asText();
                patchCmd = new ChangePortfolioMemberDirectContactCommand(hexTenancyId, companyId, directContactEmail);
                break;
            case ADD_PORTFOLIO_MEMBER:
                boolean vatEnabled = value.get("vatEnabled").asBoolean();
                vatRegNo = value.get("vatRegNo").asText();
                directAuthorisation = value.get("directAuthorisation").asBoolean();
                directContactEmail = value.get("directContactEmail").asText();
                patchCmd = new AddPortfolioMemberCommand(hexTenancyId, companyId, vatEnabled, vatRegNo, directAuthorisation, directContactEmail);
                break;
            case DELETE_PORTFOLIO_MEMBER:
                patchCmd = new DeletePortfolioMemberCommand(hexTenancyId, companyId);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported command: " + cmdtype );
        }

        return patchCmd;
    }

    public static PatchTenancyCommand getPatchTenancyCommand(
            @NonNull TenancyCommand.TenancyTenantCommandType cmdtype,
            String hexTenancyId,
            JsonNode node) throws UnsupportedOperationException, JsonProcessingException {
        PatchTenancyCommand patchCmd = null;

        switch (cmdtype) {
            case CONFIRM_TENANCY:
                patchCmd = new ConfirmTenancyCommand(hexTenancyId);
                break;
            case REJECT_TENANCY:
                patchCmd = new RejectTenancyCommand(hexTenancyId);
                break;
            case CHANGE_PORTFOLIO:
                // TODO: This is the wrong place for this logic - find a better place
                // TODO: This issue arises because we don't have dedicated deserializers for the sub-types of this class

                // Get the entity ids out of the JSON and put them in an ArrayList
                ArrayList entityIds = new ArrayList();
                ArrayNode entityIdsAsArrayNode = (ArrayNode) node.get("entityIds");
                Iterator<JsonNode> eIds = entityIdsAsArrayNode.elements();
                while (eIds.hasNext()) {
                    entityIds.add(eIds.next().asText());
                }

                // Get the portfolio details (vat settings etc) out of the JSON and put them in a Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, VatSettings> portDetails = mapper.readValue(
                        node.get("portfolioDetails").toString(),
                        new TypeReference<Map<String, VatSettings>>() {}
                );

                patchCmd = new ChangePortfolioCommand(hexTenancyId, entityIds, portDetails);
                break;
            case CHANGE_HMRC_AGENT_VAT_ENABLEMENT:
                // Get the new value out of the JSON and create a new command
                JsonNode value = node.get("value");
                boolean enabled = value.get("isHmrcVatEnabled").booleanValue();
                patchCmd = new ChangeHmrcAgentVatEnablementCommand(hexTenancyId, enabled);
                break;
            case REQUEST_HMRC_AGENT_VAT_AUTHORISATION:
                patchCmd = new RequestHmrcVatAgentAuthorisationCommand(hexTenancyId);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported command: " + cmdtype );
        }

        return patchCmd;
    }
}
