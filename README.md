# c3pro-auth - a Spring Boot auth server for the C3-PRO platform.

This authentication server is a Spring Boot / PostgreSQL port of the OAuth2 server portion of [C3-PRO/c3-pro-server](https://www.github.com/C3-PRO/c3-pro-server).  The implementation was adapted from [OAuth2Server](https://github.com/C3-PRO/c3-pro-server/blob/master/src/main/java/org/bch/security/oauth/server/OAuth2Server.java), [OAuthServerFilter](https://github.com/C3-PRO/c3-pro-server/blob/master/src/main/java/org/bch/security/oauth/server/OAuthServerFilter.java), and [RegisterServer](https://github.com/C3-PRO/c3-pro-server/blob/master/src/main/java/org/bch/security/oauth/server/RegisterServer.java).

### Differences from C3-PRO
This implements the client-credentials OAuth2 flow as described with the exception of the use of "Authorization: Basic" rather than "Authentication: Basic" header while retrieving the authentication token.

The authentication database which stores the client credentials and manages access tokens for the resource server is configured to use PostgreSQL rather than Oracle.

Also, upon 

```
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store
Pragma: no-cache
{
  "access_token":"MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3",
  "token_type":"bearer",
  "expires_in":3600,
}
```
