---
title: Building PMD General Info
tags: [devdocs]
permalink: pmd_devdocs_building_general.html
author: Andreas Dangel <andreas.dangel@pmd-code.org>
last_updated: January 2025 (7.10.0)
---

## Before Development

1. Ensure that [Git](https://git-scm.com/) and Java JDK >= 11 are installed.  
   You can get a OpenJDK distribution from e.g. [Adoptium](https://adoptium.net/).  
   **Note:**  While Java 11 is required for building, running PMD only requires Java 8.
2. Fork the [PMD repository](https://github.com/pmd/pmd) on GitHub as explained in [Fork a repository](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo).
3. Clone your forked repository to your computer:
   ```shell
   git clone git@github.com:your_user_name/pmd.git --depth=10 --no-tags
   ```
4. Clone additionally the [build-tools repository](https://github.com/pmd/build-tools). It contains some settings, we'll later use for configuring IDE:
   ```shell
   git clone git@github.com:pmd/build-tools.git
   ```
5. To make sure your Maven environment is correctly setup, we'll build pmd once:

   ```shell
   cd pmd
   ./mvnw clean verify -DskipTests
   ```

   This will help with Maven IDE integration. It may take some time, because it will download all dependencies,
   so go brew some coffee to get ready for the steps to come.

{%capture notetext%}
This only clones the last ten commits and not the whole PMD repository. This makes it faster, as much less data needs
to be downloaded. However, the history is incomplete. If you want to browse/annotate the source with the complete
commit history locally, either clone the repo without the "depth" option or convert it to a "full" clone using
`git fetch --unshallow`.
{%endcapture%}
{%include note.html content=notetext %}

## Reproducible Builds

Since PMD 6.24.0, we are creating [Reproducible Builds](https://reproducible-builds.org/). As we use
[Maven](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) for building, the following
limitations apply:

*   Generally give **different results on Windows and Unix** because of different newlines.
    (carriage return linefeed on Windows, linefeed on Unixes).

    We build our releases under **Linux** on [Github Actions](https://github.com/pmd/pmd/actions).

*   Generally depend on the **major version of the JDK** used to compile. (Even with source/target defined,
    each major JDK version changes the generated bytecode.).

    We build our releases using OpenJDK 11.

You can check the reproducible build status here: <https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/net/sourceforge/pmd/README.md>

## How to build the documentation?

    cd docs
    bundle install # once
    bundle exec jekyll build

You'll find the built site in the directory `_site/`.

For more info, see [README in docs directory](https://github.com/pmd/pmd/tree/main/docs#readme).

## How to test SNAPSHOT builds?

Every push to our main branch creates a new SNAPSHOT build. You can download the binary distribution
from <https://sourceforge.net/projects/pmd/files/pmd/>. The binary distribution ZIP file behaves exactly
the same as the real release: Just unzip it and execute PMD.

If you integrate PMD as a dependency in your own project, you can also reference the latest SNAPSHOT
version. However, you also need to configure an additional Maven Repository, as the SNAPSHOTS are not published
in Maven Central.

Use the Central Portal Snapshot repository url: `https://central.sonatype.com/repository/maven-snapshots/`. For Maven
projects, this can be configured like:
```xml
<repositories>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

Have a look at <https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/net/sourceforge/pmd/pmd/> to see which
SNAPSHOT versions are available. Note that old SNAPSHOT versions might be removed without prior notice.

See also <https://central.sonatype.org/publish/publish-portal-snapshots/>.

## General Development Tips

* Use a IDE, see one of the other guides
  * [Building PMD with IntelliJ IDEA](pmd_devdocs_building_intellij.html)
  * [Building PMD with Eclipse](pmd_devdocs_building_eclipse.html)
  * [Building PMD with Netbeans](pmd_devdocs_building_netbeans.html)
  * [Building PMD with VS Code](pmd_devdocs_building_vscode.html)
* [Contributing via GitHub and Pull Requests](pmd_devdocs_contributing.html#pull-requests)
* Keep your fork up-to-date
  * You can do this either via GitHub's Web UI
  * Or manually:
    ```shell
    git remote add upstream git@github.com:pmd/pmd.git
    git checkout main
    git pull --ff-only upstream main
    git push origin
    ```
  * See also [Newcomers' Guide](pmd_devdocs_contributing_newcomers_guide.html)
* Always create a dev branch when you are going to commit something,
  so that you can easily create a PR later on.
* Build a single module only: Always (re)building the complete PMD source takes quite a while. If you only
  change one module, you build and test just this single module to speed up development. Eg. if you changed
  a rule in pmd-apex only, you can just build this:
  ```shell
  ./mvnw verify -pl pmd-apex
  ```
  **Caveats:** We have some integration tests, that run only after all modules have been built. You could
  break these without noticing.  
  **Note:** In our CI (via GitHub Actions) we always build the complete project.
