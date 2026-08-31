# Minimal DI sandbox

This module is a deliberately small dependency injection experiment covering
only the features currently used by `cms-server`:

- constructor injection using `jakarta.inject.Inject`
- named dependencies using `jakarta.inject.Named`
- singleton scope using `jakarta.inject.Singleton`
- bindings to implementations, instances, and providers
- provider methods and separate modules
- parent/child injectors
- just-in-time construction of concrete classes

`@Provides` is the only custom annotation because Jakarta Inject has no
equivalent. There is intentionally no classpath scanning, multibinding, AOP,
lifecycle handling, or custom scopes.
Field and method injection are intentionally not supported.

```java
class Services extends AbstractModule {
    @Override
    protected void configure() {
        bind(Store.class).to(FileStore.class).singleton();
    }

    @Provides
    @jakarta.inject.Singleton
    Clock clock() {
        return Clock.systemUTC();
    }
}

Injector injector = DI.createInjector(new Services());
Application application = injector.getInstance(Application.class);
```
