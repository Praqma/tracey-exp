## Tracey Jenkins Trigger Plugin

The tracey trigger plugin add the option to add a trigger to a project. 

### Configuring the trigger

You need to add at least one server. That is done using the global configuration on Jenkins.

![Global configuration](/docs/images/global-conf.png)

Then you need to enable to tracey trigger on your job

![Job configuration](/docs/images/trigger-conf.png)

That's it. You're good to go.


### Environment

We can add the received payload to an environment variable, and we can add additional variables for use with the `Git Plugin`. This is only possible if the received message is an `EiffelSourceChangeCreatedEvent`. 

### Filters

We also provide the option to add filters to the trigger. Currently we have two types of filters. 

The first is a `Payload` filter that compares the contents of a message and parses it using the configured regex. 

The sencond filter is a `EiffelEventTypeFilter` that looks to see if the message is in the `Eiffel` format.

