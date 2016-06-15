## Tracey experiement

Currently doesn't work quite as expected...

```
WARNING: Failed to instantiate Key[type=org.jenkinsci.tracey.TraceyTrigger$TraceyTriggerDescriptor, annotation=[none]]; skip
com.google.inject.ProvisionException: Guice provision errors:

1) Error injecting constructor, java.lang.NoClassDefFoundError: com/rabbitmq/client/Consumer
  at org.jenkinsci.tracey.TraceyTrigger$TraceyTriggerDescriptor.<init>(TraceyTrigger.java:102)

1 error
        at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:52)
        at com.google.inject.Scopes$1$1.get(Scopes.java:65)
        at hudson.ExtensionFinder$GuiceFinder$FaultTolerantScope$1.get(ExtensionFinder.java:428)
        at com.google.inject.internal.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:41)
        at com.google.inject.internal.InjectorImpl$3$1.call(InjectorImpl.java:1005)
        at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1051)
        at com.google.inject.internal.InjectorImpl$3.get(InjectorImpl.java:1001)
        at hudson.ExtensionFinder$GuiceFinder._find(ExtensionFinder.java:390)
        at hudson.ExtensionFinder$GuiceFinder.find(ExtensionFinder.java:381)
        at hudson.ClassicPluginStrategy.findComponents(ClassicPluginStrategy.java:388)
        at hudson.ExtensionList.load(ExtensionList.java:349)
        at hudson.ExtensionList.ensureLoaded(ExtensionList.java:287)
        at hudson.ExtensionList.iterator(ExtensionList.java:156)
        at hudson.diagnosis.NullIdDescriptorMonitor.verify(NullIdDescriptorMonitor.java:68)
        at hudson.diagnosis.NullIdDescriptorMonitor.verifyId(NullIdDescriptorMonitor.java:89)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at hudson.init.TaskMethodFinder.invoke(TaskMethodFinder.java:106)
        at hudson.init.TaskMethodFinder$TaskImpl.run(TaskMethodFinder.java:176)
        at org.jvnet.hudson.reactor.Reactor.runTask(Reactor.java:282)
        at jenkins.model.Jenkins$7.runTask(Jenkins.java:905)
        at org.jvnet.hudson.reactor.Reactor$2.run(Reactor.java:210)
        at org.jvnet.hudson.reactor.Reactor$Node.run(Reactor.java:117)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.NoClassDefFoundError: com/rabbitmq/client/Consumer
        at java.lang.Class.getDeclaredMethods0(Native Method)
        at java.lang.Class.privateGetDeclaredMethods(Class.java:2701)
        at java.lang.Class.privateGetMethodRecursive(Class.java:3048)
        at java.lang.Class.getMethod0(Class.java:3018)
        at java.lang.Class.getMethod(Class.java:1784)
        at hudson.model.Descriptor.<init>(Descriptor.java:284)
        at hudson.triggers.TriggerDescriptor.<init>(TriggerDescriptor.java:46)
        at org.jenkinsci.tracey.TraceyTrigger$TraceyTriggerDescriptor.<init>(TraceyTrigger.java:102)
        at org.jenkinsci.tracey.TraceyTrigger$TraceyTriggerDescriptor$$FastClassByGuice$$ab2800f5.newInstance(<generated>)
        at com.google.inject.internal.cglib.reflect.$FastConstructor.newInstance(FastConstructor.java:40)
        at com.google.inject.internal.DefaultConstructionProxyFactory$1.newInstance(DefaultConstructionProxyFactory.java:61)
        at com.google.inject.internal.ConstructorInjector.provision(ConstructorInjector.java:108)
        at com.google.inject.internal.ConstructorInjector.construct(ConstructorInjector.java:88)
        at com.google.inject.internal.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:269)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:46)
        at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1058)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:40)
        ... 27 more
Caused by: java.lang.ClassNotFoundException: com.rabbitmq.client.Consumer
        at jenkins.util.AntClassLoader.findClassInComponents(AntClassLoader.java:1376)
        at jenkins.util.AntClassLoader.findClass(AntClassLoader.java:1326)
        at jenkins.util.AntClassLoader.loadClass(AntClassLoader.java:1079)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        ... 44 more

```