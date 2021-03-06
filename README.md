# demo-oauth2-spring-security

A simple Password Flow demo with Spring Security OAuth 2

## Testing

You can use [Postman](https://www.getpostman.com/) to test it.

### First you'll need to get the access_token

1. Create a POST request for the address: http://localhost:9080/datagenerator/oauth/token

2. You have to pass Basic Auth too, this is the client credentials, not the user. 
In this example the username is "client" and password "clientpassword" (without quotes).

The authorization header will looks like: 
Authorization : Basic Y2xpZW50OmNsaWVudHBhc3N3b3Jk

3. Set the Content-Type header to application/x-www-form-urlencoded

4. Set the body

You'll have to choose x-www-form-urlencoded and set the values:

client_id   client

username    user

password    user

grant_type  password

When you hit the send button, you'll get something like that:

```json
{
  "access_token": "bd999429-898b-4201-908e-40e846ec0105",
  "token_type": "bearer",
  "expires_in": 3599,
  "scope": "read write"
}
```

### Calling the API

Now you are able to call the API using the access token

1. Create a GET request in Postman with the URL http://localhost:9080/datagenerator/extractor

2. Set the Authorization header with Bearer <token>

Key: Authorization

Value: Bearer bd999429-898b-4201-908e-40e846ec0105

That's all! When you hit the Send button, you'll receive:

```json
[
  {
    "customers": {
        "customerNumber": "int(11)",
        "customerName": "varchar(50)",
        "contactLastName": "varchar(50)",
        "contactFirstName": "varchar(50)",
        "phone": "varchar(50)",
        "addressLine1": "varchar(50)",
        "addressLine2": "varchar(50)",
        "city": "varchar(50)",
        "state": "varchar(50)",
        "postalCode": "varchar(15)",
        "country": "varchar(50)",
        "salesRepEmployeeNumber": "int(11)",
        "creditLimit": "decimal(102)",
        "PK": "customerNumber",
        "FK1->salesRepEmployeeNumber": "employees(employeeNumber)"
    }
    .
    .
    .
]
```
 