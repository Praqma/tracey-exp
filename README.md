## Tracey Jenkins Trigger Plugin

This plugin allows you to trigger jobs using [RabbitMQ](https://www.rabbitmq.com/), and also provides additonal integrations to respond to messages in the Eiffel messagaging format. Which is documented [here](https://github.com/Ericsson/eiffel).   

### Configuring the trigger

You need to add at least one server. That is done using the global configuration on Jenkins.

![Global configuration](/docs/images/global-conf.png)

Then you need to enable to tracey trigger on your job

![Job configuration](/docs/images/trigger-conf.png)

That's it. You're good to go.

#### Configuring the trigger using the Jenkins Job DSL plugin

You can configure the Tracey trigger using Jenkins Job DSL:

```
job('tracey-job') {
    triggers {
        tracey('exchangeName', 'traceyHostName') {
            injectEnvironment {
                payloadKey 'MY_PAYLOAD_ENV_KEY'
                payloadInjection "FOO (foo)*", "BAR [BAR]*"
                injectGitVariables true
            }
            filters {
                payloadRegex '\\d{5}'
                eiffelEventType {
                    artifactPublished()
                    sourceChangeCreated()        
                }
            }
        }
    }
}
```

### Environment

You can add the content of the recieved message as an environment variable to your build by checking the `Add payload to environment` checkbox. This will unfold two additonal options.

 - `Inject variables for Git Plugin` _requires an EiffelSourceChangeCreatedEvent type of payload_. See more [here](https://github.com/Ericsson/eiffel/blob/master/eiffel-vocabulary/EiffelSourceChangeCreatedEvent.md)
 
 - `Add payloads to environemnt`. A list of environment variables to add to the build. Syntax is explained in the help for the input field.
 
### Filters

We also provide the option to add filters to the trigger. Currently we have two types of filters. 

The first is a `Payload` filter that compares the contents of a message and parses it using the configured regex. If the payload matches the provided regex, then this message triggers the job, otherwise not.  

The sencond filter is a `EiffelEventTypeFilter` that looks to see if the message is in the `Eiffel` format. More detail about this format can be found [here](https://github.com/Ericsson/eiffel). 

Filters are applied in order. If one of your choices rejects the payload and the message received, the message will not trigger this project. 