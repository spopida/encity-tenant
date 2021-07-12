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

## HMRC VAT Authorisation
  
  When the HMRC VAT Enablement flag is modified to `true` we need to figure out whether to do anything about acquiring an access token and/or authorisation.  When HMRC VAT is *authorised* the token is created.  Thereafter, it needs to be refreshed on a regular basis (as it only lasts for 4 hours), so this will result in a different refresh event.
  
  When we request authorisation, we need to record something a) because there will be a UUID and expiry associated with the authorisation request and b) because we need to know if there is a request pending to avoid sending multiple requests.  We could issue the request as an integral part of the HMRC_VAT_ENABLEMENT_CHANGED event, or we could de-couple it into a separate event.  I prefer the latter because we won't always need to issue a request when the VAT enablement is changed, and it seems easier to associate a discrete event with the subsequent authorisation.
  
  The request will result in a logical 'pending' status on the tenancy, however this could expire before an authorisation event.  So, we need to distinguish between 
  - whether or not authorisation is needed (i.e. if the access token is non-existing or is older than 18 months)
  - whether or not authorisation has been requested (i.e. if a request has been sent that has not been actioned)
  - whether or not the request is still actionable (i.e. if the request has not yet expired)
  
  Also we need functionality in two different places: when a tenancy enablement is modified, and in the dashboard.
  
  ### Dashboard Functionality
  
  #### Warning Light
  
  The dashboard will be augmented with a Warning Light.  
  IF 
  
    - HMRC VAT is enabled AND 
    - at least one company has the VAT flag set and a valid VRN AND 
    - the access token is either missing or looks old, 
  
  THEN
  
    - we will show a warning light (button).
  
  When the button is clicked, the user will be referred to account settings and given a link to go there; this could be in a modal dialog or an embedded (collapsable) field.
  
  If 
    - there is a pending authorisation request
  THEN
    - the warning (if it exists) can be amber, not red
  
  If there are VAT-eligible companies, and there is an access token then we will only try to use it if it looks like it is still valid.  It might fail anyway.  If it fails, we will update a generic messages component.
  
  If automatic_reauthorisation_requests is set to true for the tenancy, then we will automatically issue a reauthorisation request if it is needed and there isn't already one pending, otherwise the user will have to go to the account settings page (do this later)
  
  The colour of the warning will depend on whether there is a pending request. If there is NOT it will be red, otherwise it will be amber
  
  ### Account Settings
  
  In account settings we will use the same logic to display a warning light.
  
  The light will come on if enablement is set to true for the first time because there will be no token.  But here it can be amber because we know a pending request will be automatically created - so really the warning is just saying VAT retrieval won't work until the request is authorised.  We can use the same colouring logic in the dashboard, because by the time anyone gets there a request will have been created 
  
  If the hmrc_vat_authorisation_warning status is set, we will check for a pending authorisation request
  
  if there is no pending request (and the warning status is set) then we will offer a button to (re)issue a request - but only if there is already a stale access_token: first time request is taken care of automatically.
  
  When an hmrc_enablement is being changed, if it is being switched to ON, and there is no access_token (i.e first time) then we will 
  *automatically* issue a first-time authorisation request.  Otherwise re-authorisation will be manual - unless the automatic_reauthorisation_requests flag is set to true, in which case it's the dashboard that does the work.
  
  The auto-issued first-time request is generated by the server.  We will NOT stick it in the multi-patch request.  Instead, we will subscribe to the enablement event.
  
  The manual button will issue a single request immediately; it may change the status on the screen, because it could affect the colour of the warning
  
  ### Refresh Logic
  
  Refresh should be automatic and silent (?) - it will only be triggered by the dashboard.  It is not an error - it's a perfectly normal (and quite frequent) side effect of retrieval.
  
  ### Summary of changes
  
  ----------------------------------------------------------------------------------------------------
  - inclusion of an optional hmrc_vat_access_token object (structure) in the tenancy.
  - derivation of a hmrc_vat_access_token_warning status on the tenancy - this should NOT be persisted
  - derivation of an authorisation pending flag on the tenancy - there is no AUTHORISED event after the latest REQUESTED event and the latter is NOT expired 
  - attempt to retrieve vat obligations if the access token looks valid in dashboard - enhance the HMRC service with GET including access token!
  - auto refresh in dashboard - requires updates to HMRC service - probably a POST ??, then a PATCH to the tenancy service with the result (then re-do the failed get)
  - inclusion of warning light on dashboard
  ----------------------------------------------------------------------------------------------------
  - inclusion of warning light on account settings
  - inclusion of re-authorise button on account settings - generate a patch with an auth request in it
  - auto-generate first-time authorise request in tenancy server - listen for enablement event and generate request with UUID and expiry
  - subscribe to authorise request in notification service - generate an email
  - new account settings screen to operate in authorise mode 
  - try to retrieve the unexpired authorise request and patch an authorise command back to the server - requires back and forth to HMRC Service!
    - First re-direct to HMRC authorise endpoint (with callback URL)
    - HMRC Service subscribes to AUTHORISE event, creates the access token, and publishes it
    - Tenancy Service subscribes to HMRC_VAT_ACCESS_TOKEN_CREATED event and stores it in the database!
  ----------------------------------------------------------------------------------------------------
  ### Relevant Events
  
  - HMRC_VAT_ENABLEMENT_CHANGED (whenever)
  - HMRC_VAT_AUTHORISATION_REQUESTED (rare - approx every 18 months, generated by account settings and/or tenancy service)
  - HMRC_VAT_AUTHORISED (rare - generated by new screen)
  - HMRC_VAT_ACCESS_TOKEN_CREATED (rare - generated by HMRC service)
  - HMRC_VAT_ACCESS_TOKEN_REFRESHED (common - every 4 hours, generated by dashboard)

  
  
  


