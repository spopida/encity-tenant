# Encity Tenancy Service Design Notes

## Tenancy Confirmation Workflow

### New Tenancy Creation

A new tenancy is created with a POST command, that results in a published tenancy-created-event.
The event is received by the notificaiton service, which emails the authorised contact.  The
email contains a confirmation URL.  The idea is that when the recipient clicks on the URL, this will visit an
encity page to retrieve the tenancy referenced in the URL and allow them to confirm it

The SPA will retrieve the given tenancy by doing a GET and display it.  In the response, 
HATEOAS links will be contained.  **If** the tenancy is still unconfirmed, **and** the expiry 
time has not been reached, **then** the HATEOAS links will contain both confirm and reject
links.  If the tenancy has already been confirmed, then the user will be told, and if 
it has expired, then they will also be told - they will not be able to do anything - the HATEOAS
links will be empty...or maybe they could go to the encity home page with an 'OK' button?

To support this, the server will include warning messages if necessary, to prevent this
logic from creeping into the UI.

The confirm and reject links will be both need to be used with PUT operations.  These will append
the necessary items in the database, and the notification service will detect a published 
tenancy-confirmed event, whereupon it will email the authorised contact (again).  (They will 
need to validate the operations first, however).

A user service will also detect this event, and it's job will be to create a new user for 
the admin user.  The event needs to contain this person's details...and this means the inflated
tenancy needs to hold them.  We don't really want to have to go back to a different event, and 
from there, navigate to the command.


### New Tenancy Confirmation

The link embedded in an email to the Tenancy authoriser looks something like this:
```
https://encity.co.uk/tenancy/YBGg2Z3p1Vv-dk67?action=confirm&uuid=61187015-363d-4a72-b9e5-103a7c232ae5
```
This will invoke the UI service with a GET, not the tenancy service.  The UI then needs to translate this into a GET on 
the tenancy service to retrieve the details of the tenancy identified in the URL.  in doing the retrieval, the 
service needs to check on the state of the tenancy, and respond accordingly to the UI.  **IF** the U I is allowed
to proceed with confirmation (or rejection) then it will go ahead, and issue a PUT to the service.  The GET URL sent by
the UI to the service will look something like this:
```
https://encity.co.uk:3002/tenancy/YBGg2Z3p1Vv-dk67?action=confirm&uuid=61187015-363d-4a72-b9e5-103a7c232ae5
```
The service will 

a) translate the base64url identity into hex (?)
b) retrieve the (logical) tenancy entity
c) check that action to confirm is allowed (this implies it could also be rejected)
d) check that the confirm action has not expired
e) check that the confirm uuid matches
f) return allowable actions in the links object (including base64url ids)
g) or return the appropriate status code, including any warnings

If the entity can be retrieved, but not actioned, then this is still a 200, but warnings will be embedded in the
response

The UI will then check the links.  If neither confirm nor reject are there, then it will check the warnings, otherwise
it will enable the appropriate buttons to support a PUT (both confirm and reject should be enabled).

the PUT action will look like this:
```
https://encity.co.uk:3002/tenancy/YBGg2Z3p1Vv-dk67?action=<action>&uuid=61187015-363d-4a72-b9e5-103a7c232ae5
```
The <action> will be either confirm or reject.

It will repeat most of the above checks (things may have changed!), and if it's OK to proceed, it will create a new
tenancy-confirmed-event or tenancy-rejected-event and insert it in the database.  Then it will publish this event.

The notification service should receive these events and email the authoriser advising of the action

Also a user service should subscribe to tenancy-confirmed-event, and set up a new admin user (the event will need
to contain the admin user details, which means they'll also need to be in the snapshot schema!).  Upon setting up the new
admin user, the user service will (of course) publish an event, and it'll need to go through a similar confirm / reject
workflow.  See the user service for more on this though.



