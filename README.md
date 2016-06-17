## Tracey experiment

### Install dependencies

Install the dependencies described in [tracey-cli](https://github.com/Praqma/tracey-cli) (Core and Broker)

### FIXME's

- How do you actually write a trigger plugin? The docs, and the plugins that do are super hard to read and understand?
- The trigger is `blocking` in the current implementation. This needs to be fixed. 
- Timing: We need to do the following.
 - 1 Job, 1 trigger. OnSave - Stop previous. Configure new, and replace old reference with a new instance of the broker.
- **SERIOUS BUG 1**: When you save your job, it adds a new trigger. If you save again, it will add another trigger...duplication. 
- **SERIOUS BUG 2**: Trigger is **NOT** loaded when Jenkins starts. You'll have to save jobs where using trigger again. 
 

Thats basically it...
