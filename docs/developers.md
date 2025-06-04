# Developer Area

## Local Testing

For local testing please run an author and a publish instance on default ports (4502/4503).
The dispatcher can be started with the dispatcher SDK from Adobe (part of cloud SDK).

* Link its "src" directory to this repo "dispatcher/src"
* Run `./bin/docker_run.sh src host.docker.internal:4503 8080`

If you need to clean the cache delete the content of the "cache" directory.

Required entries in "/etc/hosts":

```
127.0.0.1       ca.vanity.local
127.0.0.1       us.vanity.local
```

Test URLs:

* http://us.vanity.local:8080/wow
* http://ca.vanity.local:8080/wow

## Publish a Release

* Update CHANGES.md if needed
* `mvn release:clean release:prepare`
* `mvn release:perform`
* Add GitHub release from tag https://github.com/IBM/aem-tenant-specific-vanity-urls/tags
  * attach "all" and "examples" packages

## Requirements for Release Publishing

Follow these steps to get authorized to perform a release.

* Create a PGP key: `gpg --gen-key`
* Upload your public key to e.g. http://keyserver.ubuntu.com:11371/
* `brew install pinentry-mac`
* Update your "~/.zshrc" and add `export GPG_TTY=$(tty)`, then close all open terminals and exit the terminal app
* Create/update "~/.gnupg/gpg-agent.conf" and add `pinentry-program /opt/homebrew/bin/pinentry-mac`
* `gpgconf --kill gpg-agent`
* Request publish rights for Maven Central at "com.ibm.aem" (https://central.sonatype.org/register/central-portal/).
  The request needs to be approved by someone who already has this right.
* Follow https://central.sonatype.org/publish/generate-portal-token/ to get your user token and add it to your .m2/settings.xml file:

```
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>token-id</username>
      <password>token-value</password>
    </server>
  </servers>
</settings>
```
