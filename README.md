[![Build Status](https://api.travis-ci.org/Praqma/tracey-jenkins-trigger-plugin.svg?branch=master)](https://travis-ci.org/Praqma/tracey-jenkins-trigger-plugin)
---
maintainer: andrey9kin, alexsedova
---


## Usage

Download the snapshot release from Github releases and install it on Jenkins by uploading the hpi file.

Plugin is not yet officially released through the Jenkins CI update center.

## RabbitMQ Jenkins Trigger for Tracey

This plugin allows you to trigger jobs using [RabbitMQ](https://www.rabbitmq.com/).

### Configuring the trigger

You need to add at least one server. That is done using the global configuration on Jenkins. It is only RabbitMQ host available now

![Global configuration](/docs/images/global-config.png)

Then you need to enable RabbitMQ trigger on your job

![Job configuration](/docs/images/trigger.png)

That's it. You're good to go.

#### Configuring the trigger using the Jenkins Job DSL plugin

You can configure the Tracey trigger using Jenkins Job DSL:

```
job('tracey-job') {
    triggers {
        tracey('exchangeName', 'exchangeType', 'rabbitmq-id') {
            injectEnvironment {
                payloadKey 'MY_PAYLOAD_ENV_KEY'
                payloadInjection "FOO (foo)*", "BAR [BAR]*"
            }
            filters {
                payloadRegex '\\d{5}'
                payloadKeyValue('name', 'value')
                payloadJSONRegex '$..*[?(@.id == 2)]'
            }
        }
    }
}
```

The rabbitmq host id is the unique identifier assigned to the configured host. You can set this value yourself by expanding the 'Advanced' tab in the host configuration.

### Environment

You can add the content of the received message as an environment variable to your build by checking the `Add payload to environment` checkbox.

 - `Environment variable name to store payload` Name of an environment variable to store payload received from the message that triggered this job.
 - `Regex-based extractions` This is where you define your custom injections. Plugin will inject one environment variable per capture group and one environment variable that contains the whole selection
     I.e. Let’s take message below as example:
     `{ "commitId": "05e9017c42a7d2a974690f17dcde50d1e2ed86a1", "branch": "master", "repoName": "project", "repoUri": "https://github.com/path-to-project" }`
     if we apply the following extraction:
     `BRANCH_FROM_MESSAGE \"branch":\s+\"([^"']+)\" `
     as result we will get two env variables injected:
     `BRANCH_FROM_MESSAGE `contains `branch = master` (the whole match)
     and
     `BRANCH_FROM_MESSAGE_1` contains `master` (first match group)

### Filters

We also provide the option to add filters to the trigger. Currently we have three types of filters.
![Filters](/docs/images/filters.png)

The first is a `Payload regex` filter that compares the contents of a message and parses it using the configured regex. If the payload matches the provided regex, then this message triggers the job, otherwise not.

The second filter is a `JSON Basic filter` that looks to see if json-format message is match key:value format.

The third filter is a `JSON Regex filter` that looks to see if json-format message is match regex expression. More information you may find [here](https://github.com/jayway/JsonPath)

Filters are applied in order. If one of your choices rejects the payload and the message received, the message will not trigger this project.

### Self contained demo project

We also provide a self contained demo project for tracey. This demo requires docker with docker compose to work. You can find the repository [here](https://github.com/Praqma/tracey)

In order to get this up and running:

    git clone git@github.com:Praqma/tracey.git
    cd tracey
    docker-compose up

That should do it. Now you have a jenkins running on port 8080, a configured rabbitmq server and a configured job. Login details are (jenkins/demo).


## Releasing

To release a new version of this CLI on Github release you need to tag the commit to release. This will be picked up by Travis CI.

### Github auth for Travis release

Release is done a ReleasePraqma user and was securely created using `travis setup releases`

    $ travis setup releases
    Detected repository as Praqma/tracey-protocol-eiffel-cli-generator, is this correct? |yes| yes
    Username: ReleasePraqma
    Password for ReleasePraqma: **********
    File to Upload: build/libs/tracey-protocol-eiffel-cli-generator.jar
    Deploy only from Praqma/tracey-protocol-eiffel-cli-generator? |yes| yes
    Encrypt API key? |yes| yes
