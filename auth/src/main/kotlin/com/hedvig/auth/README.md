# com.hedvig.auth

The `com.hedvig.auth` package handles users and their credentials. A user is any data subject that can authenticate and
interact with the Hedvig system. A user is typically associated with a specific member, but that member does not
necessarily need to be a member with a signed contract.

### Example usage

```kotlin
import com.hedvig.auth.services.UserService

val userService = UserService()

val user = userService.findOrCreateUserWithCredential(
    UserService.Credential.SwedishBankId(
        personalNumber = "190001010101"
    )
)

if (user != null) {
    // Succeeded
} else {
    // Failed
}
```
