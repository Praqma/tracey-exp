## Tracey experiment

### Install dependencies

Install the dependencies described in [tracey-cli](https://github.com/Praqma/tracey-cli) (Core and Broker)

### FIXME's

- How do you actually write a trigger plugin? The docs, and the plugins that do are super hard to read and understand?
- The trigger is `blocking` in the current implementation. This needs to be fixed. 
- Timing: We need to do the following.
 - 1 Job, 1 trigger. OnSave - Stop previous. Configure new, and replace old reference with a new instance of the broker.

Thats basically it...