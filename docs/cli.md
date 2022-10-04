# Command Line and Shell

Enhanced Mule Tools provides a command tool ( emt ) which can be used to perform various operations. This can be
installed
on your system as per instructions under [setup](setup.md).

The emt command tool also has the ability to run a custom shell by running the `emt shell` command. This shell has the
benefit of provide auto-completion of commands.

Additionally, instead of installing the cli tool it is possible to access the same capabilities by using shell goal of
the maven plugin. ie:

```shell
mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:{{ emtVersion }}:shell
```

This maven goal can also be used to run commands using the `cmd` parameter

```shell
mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:{{ emtVersion }}:shell -Dcmd=keygen
```

### Base flags

```shell
Usage: emt [--version] [-p=<profileName>] [[-upw=<upw> <upw>]... [-bt=<bearer>]
[-cc=<clientCreds> <clientCreds>]...] [COMMAND]
-bt, --bearer=<bearer>
Bearer token credentials
-cc, --credential-credentials=<clientCreds> <clientCreds>
Credential Credentials
-p=<profileName>    Profile
-upw, --username-password=<upw> <upw>
Username / Password credentials
--version       display version info
```

Options:

| Option | Description                                                                                                                     |
|--------|---------------------------------------------------------------------------------------------------------------------------------|
| -p     | This is used to specify which profile to use when executing the command                                                         |
| -upw   | Authenticate using anypoint username/password credential. The first parameter is the username, the second is the password       |
| -bt    | Used to specify an anypoint bearer token                                                                                        |                                                                                 |
| -cc    | Authenticate using anypoint username/password credential. The first parameter is the client id, the second is the client secret |
| -d     | Enable debug logging                                                                                                            |

### Configuration

#### emt profile

```shell
Usage: emt profile [-c] [<profile>]
Change or show active profile
      [<profile>]   profile to activate
  -c, --create      Create profile if required
```

This command can be used to view which profile is currently active by not specifying any parameter. ie:

```shell
> emt profile
Active profile: default
```

Or it can be used to change the active profile by adding the profile parameter

```shell
> emt profile otherprofile
Active profile: otherprofile
```

This assumes that profile already exists. To create a profile that doesn't already exist, use the -c flag

```shell
> emt profile -c otherprofile
Active profile: otherprofile
```

#### emt config upw

```shell
Usage: emt config upw <username> <password>
Set username/password authentication in configuration
      <username>   username
      <password>   password
```

This command is used to set authentication to use an anypoint username/password.

#### emt config bearer

```shell
Usage: emt config bearer <bearer>
Set username/password authentication in configuration
      <bearer>   Bearer token
```

This command is used to set authentication to use an anypoint bearer token

#### emt config env

```shell
Usage: emt config env <env>
Set default environment in configuration
      <env>   Default environment name or id
```

This command is used to set the default environment that 
