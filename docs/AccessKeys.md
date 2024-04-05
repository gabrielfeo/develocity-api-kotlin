# Access key / API token

[All API requests require authentication][1]. Provide a valid access key of your Develocity instance
as the `DEVELOCITY_API_TOKEN` environment variable.

## How to get an access key

1. Sign in to Develocity (with a user that has “Export build data” permission)
2. Go to "My settings" from the user menu in the top right-hand corner of the page
3. Go to "Access keys" from the sidebar
4. Click "Generate" on the right-hand side
5. Set key as the `DEVELOCITY_API_TOKEN` environment variable when using the library

## Migrating from macOS keychain support

This library used to support storing the key in the macOS keychain as `gradle-enterprise-api-kotlin`.
This feature was deprecated in 2023.4.0, then removed in 2024.1.0. You may use the method of your choice
(secret managers, password manager CLIs, etc.) to store and retrieve the key to an environment.

If you used the key from keychain and need a drop-in replacement:

```
# Create an alias in your shell to fetch the key from keychain
echo 'alias dat="security find-generic-password -w -a "$LOGNAME" -s gradle-enterprise-api-kotlin"' >> ~/.zshrc

# Retrieve it to the environment variable before running the program
DEVELOCITY_API_TOKEN="$(dat)" ./my-script.main.kts
DEVELOCITY_API_TOKEN="$(dat)" jupyter lab
DEVELOCITY_API_TOKEN="$(dat)" idea my-project
```

[1]: https://docs.gradle.com/enterprise/api-manual/#access_control
