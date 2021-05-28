package uk.co.encity.tenancy.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import uk.co.encity.tenancy.entity.Tenancy;
import uk.co.encity.tenancy.events.TenancyEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public static PatchTenancyCommand getPatchTenancyCommand(
            @NonNull TenancyCommand.TenancyTenantCommandType cmdtype,
            String hexTenancyId,
            JsonNode node)
    {
        PatchTenancyCommand patchCmd = null;

        switch (cmdtype) {
            case CONFIRM_TENANCY:
                patchCmd = new ConfirmTenancyCommand(hexTenancyId);
                break;
            case REJECT_TENANCY:
                patchCmd = new RejectTenancyCommand(hexTenancyId);
                break;
            case CHANGE_PORTFOLIO:
                ArrayList entityIds = new ArrayList();
                ArrayNode entityIdsAsArrayNode = (ArrayNode) node.get("entityIds");
                Iterator<JsonNode> eIds = entityIdsAsArrayNode.elements();
                while (eIds.hasNext()) {
                    entityIds.add(eIds.next().asText());
                }
                //List<String> entityIds = node.findValuesAsText("entityIds");
                patchCmd = new ChangePortfolioCommand(hexTenancyId, entityIds);
        }

        // TODO: should really throw an exception for unsupported command type instead of leaving it to the caller
        // to detect a null command

        return patchCmd;
    }
}
