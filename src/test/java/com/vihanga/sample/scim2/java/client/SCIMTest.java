package com.vihanga.sample.scim2.java.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

/**
 * Test class to demonstrate SCIM2 Java API Usage.
 */
public class SCIMTest {

    public static void main(String[] args) {

        try {
            String propertyFilePath = "src/main/resources/app.properties";
            PropertyResourceBundle properties = new PropertyResourceBundle(new FileInputStream(propertyFilePath));

            SCIM2Client scim2Client = new SCIM2Client(properties);

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
