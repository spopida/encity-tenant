# Adding a new PATCH command

1. Add the new command string to the enum in `patch-tenancy-command.json`
2. Create a new event class with an accompanying event serializer
3. Update TenancyEventType to add an enum entry
4. Update TenancyTenantCommandType and the static initializer in `TenancyCommand.java`
5. Update `PatchTenancyCommand.getPatchTenancyCommand()`
6. Create a new command class for the command
7. Changes entity/vatSettings.java ???
8. Add an event model to `service/repositories/MongoDBTenancyRepository`
9. change `TenancyService.applyCommand()`
