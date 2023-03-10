https://anypoint.mulesoft.com/exchange/portals/anypoint-platform/f1e97bc6-315a-4490-82a7-23abe036327a.anypoint-platform/exchange-experience-api/

Please, follow these steps:

1. Go to the link above.
2. Log in with your user (in case you are not yet logged in).
3. Click on Getting Started section.
4. Run the first snippet (play snippet button) to create Client.
5. Run the second snippet (play snippet button) to get the Oauth token).
6. Run the last snippet to get the assets.
7. In the results you will find the assets for the business group.

Additionally, notice that you have three users under "ATUTestDeleteMe". So you can ask the other users to check the
assets in Exchange since, most probably, at least one of them have an asset to delete on this group.

--- 

"Instead of using "/v2/oauth2/authorize" endpoint, they can use "/v2/oauth2/authorize/<domain_name>" endpoint. This
should redirect them directly to the domain login page when the user is not logged in.

For example:
https://anypoint.mulesoft.com/accounts/api/v2/oauth2/authorize/:domain?client_id=<client_id>&scope=profile&response_type=code&redirect_uri=https://example.com&nonce=123456

The "/v2/oauth2/token" endpoint will remain the same after the User authorizes the app."
