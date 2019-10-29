# SCIM2 Java client
This is a simple Java client written to easily invoke the [SCIM2](http://www.simplecloud.info/#Implementations2) API.

##  How to use.

- Sample code snippet.
```java
public class SCIMTest {

    public static void main(String[] args) {

        try {
            SCIM2Client scim2Client = new SCIM2Client();

            String username = "vihanga";

            String scimId = scim2Client.getUserSCIMID(username);
            System.out.println("SCIM ID of the user " + username + " is: " + scimId);

            String status = scim2Client.updateUserAttribute(scimId, "name.givenName", "Vihanga Liyanage");
            System.out.println("Updating user attribute givenName... Status: " + status);

            status = scim2Client.updateExtendedUserAttribute(scimId, "country", "Sri Lanka");
            System.out.println("Updating extended user attribute country... Status: " + status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

- This client uses a property file to read all the required information to run.
A sample property file ([app.properties](src/main/resources/app.properties)) is included in the resources directory.
  - Possible values in the property file.
  
  | Property Key | Description | Sample Value |
  | ------------ | ----------- | ------------ |
  | scimEndpointPrefix | SICM2 implementation endpoint. |https://localhost:9443/scim2 |
  | adminUsername | Username to authenticate with the server. | admin |
  | adminPassword | Password to authenticate with the server. | admin |
  | trustStorePath | Path to the keystore | src/main/resources/wso2carbon.jks |
  | trustStorePassword | Password of the keystore | wso2carbon |
  
- Some servers may require to create an SSL connection to call the SCIM API.
You can add a keystore to the client so that it'll be set in JVM properties.
