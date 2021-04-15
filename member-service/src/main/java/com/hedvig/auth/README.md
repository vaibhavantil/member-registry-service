# com.hedvig.auth

The `com.hedvig.auth` package handles users and their credentials. A user is any data subject that can authenticate and interact with the Hedvig system. A user is typically associated with a specific member, but that member does not necessarily need to be a member with a signed contract.

## UserService

The `UserService` can be used to lookup (or create) a user with the provided credentials. To find a user with matching credentials you use the `findOrCreateUserWithCredentials()` method.

If a user with matching credentials is found it will be returned, but if no matching credentials are found a new user is created with the credentials provided. The new user becomes associated with the member that you specify in the `onboardingMemberId` parameter. The next time you call `findOrCreateUserWithCredentials()` with the same credentials, the previously created user is returned.

If `onboardingMemberId` is `null`, then no user will be created.

The following credential types are currently supported:

- **SwedishBankId**: An ordinary Swedish personal number is used as credential to uniquely identify a user.
- **ZignSec**: Uses the `idProviderName` (the name of the underlying identity provider) and `idProviderPersonId` (a unique identifier for a person) as credentials to uniquely identify a user. Can also include an optional personal number if SimpleSign fallback is needed (see below).

### SimpleSign
SimpleSign is a method for signing contracts without authentication. When calling `findOrCreateUserWithCredentials()` and no user with matching credentials is found, **SimpleSignConnection** will be used as a fallback method. A user will be looked up based on the personal number provided when signing with SimpleSign. This is of course only possible if the credential type used includes a personal number (so works with the current credential types, SwedishBankId and ZignSec). Note that SimpleSignConnection is not a credential in a strict sense, because the personal number is completely unauthenticated, so can not be used on its own as a credential type, only as a fallback for another authenticated credential type.

SimpleSign fallback can be turned off by setting the `simpleSignFallback = false` property on a credential type that supports it.

### Example usage

```kotlin
val user = userService.findOrCreateUserWithCredentials(
    UserService.Credentials.SwedishBankId(
        personalNumber = "190001010101"
    )
)

if (user != null) {
    // Succeeded
} else {
    // Failed
}
```

## Import of (signed) members

When a new member signs a contract the member should also automatically get a user, so she can log in to her account later. That is accomplished by listening to the `MemberSignedEvent` (et al.) and creating a new user with the correct credentials. Depending on what auth method the new member used to sign the contract this is handled by different events (and the credentials are obviously different). This is all handled by the importers in the `com.hedvig.auth.import` package.

- **SimpleSignMemberImporter**: Listens to `MemberSimpleSignedEvent` events and creates a new user with a `SimpleSignConnection`.
- **SwedishMemberImporter**: Listens to `MemberSignedEvent` and creates a new user with a `SwedishBankIdCredential`.
- **ZignSecMemberImporter**: Listens to `NorwegianMemberSignedEvent` and `DanishMemberSignedEvent` and creates a new user with a `ZignSecCredential`.
