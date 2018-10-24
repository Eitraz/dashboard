# Dashboard

Personal dashboard made with Vaadin 11 displaying Google Calendar events, weather forecast and temperatures from MQTT.

## Installation

1. Run `build.sh` to build the application, setup properties and build the docker image.
2. Run `run.sh` to create and run a the docker image and tail the log (for step 3)
3. The application will print a link to authenticate with Google Calendar. Visit the link and then copy the returned callback link.
4. Enter the docker container using `docker exec -i -t dashboard /bin/bash` and execute `curl "[link from step 3]"` to complete the Google Calender authorization.