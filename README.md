# Liferay multiple reminder questions extension

This project contains 3 modules required to override/extend the functionality
provided by the reminder question. These modules will allow you to add more than 
one reminder question. To deploy these modules, either run them with `mvn clean verify`
with a `settings.xml` that has a default profile with a `liferay.home` property.

E.g.

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <profiles>
        <profile>
            <id>default</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <liferay.home>C:\your\liferay\home\folder\bundled</liferay.home>
            </properties>
        </profile>
    </profiles>
</settings>
```
Or copy/paste the JARs in the `..\bundled\deploy` folder of your Liferay home folder

Adding the additional questions is done by means of the [Expando API](http://proliferay.com/liferay-expando-api/).
When first adding the additional columns, the permissions for the *User* and *Guest* role are set to **VIEW**, **UPDATE** and **VIEW**, respectively. 
## ForgotPasswordMVCCommands
The additional questions need to be checked when submitting the forgot password form.
This modules extends the original action performed by the forgot password form
by adding checks for the added questions. 

**NOTE** This module also assumes there's no need to keep track of the reminder attempts. 

## ForgotPasswordQuestions
This module adds the additional questions in the GUI, it overrides the `forgot_password.jsp`.

**NOTE** This module removes the captcha, which was causing problems for deployment.

## UpdateSecurityQuestions
This module allows the user to answer more reminder questions at first login.
This module contains front end modifications as well as modifications to the action.

**NOTE** This module also disables the custom question functionality to fit the needs of the project.