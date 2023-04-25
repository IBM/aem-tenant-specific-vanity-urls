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

* `mvn release:clean release:prepare`
* `mvn release:perform`

## Requirements for Release Publishing

Follow these steps to get authorized to perform a release.

* Create a PGP key: `gpg --gen-key`
* `brew install pinentry-mac`
* Update your "~/.zshrc" and add `export GPG_TTY=$(tty)`
* Create/update "~/.gnupg/gpg-agent.conf" and add `pinentry-program /opt/homebrew/bin/pinentry-mac`
* Request publish rights for Maven Central at "com.ibm.aem" via Sonatype ticket (https://issues.sonatype.org/secure/Dashboard.jspa).
 The request needs to be approved by someone who already has this right.
* Add the Sonatype credentials to your .m2/settings.xml file:

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>
```