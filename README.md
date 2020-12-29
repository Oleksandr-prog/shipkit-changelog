[![CI](https://github.com/shipkit/shipkit-changelog/workflows/CI/badge.svg)](https://github.com/shipkit/shipkit-changelog/actions)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/shipkit/shipkit-changelog/maven-metadata.xml.svg?label=Gradle%20Plugins)](https://plugins.gradle.org/plugin/org.shipkit.shipkit-changelog)

# Shipkit

## Vision

Software developers spend all their creative energy on productive work.
There is absolutely **zero** release overhead because all software is released *automatically*.

## Mission

Encourage and help software developers set up their releases to be fully automated.

# Shipkit Changelog Gradle plugin

Our plugin generates changelog based on commit history and GitHub pull requests/issues. 
Optionally, the changelog content can be posted to GitHub Releases.
This plugin is very small (<1kloc) and has a single dependency "com.eclipsesource.minimal-json:minimal-json:0.9.5".
The dependency is very small (30kb), stable (no changes since 2017), and brings zero transitive dependencies.

Example ([more examples](https://github.com/shipkit/shipkit-changelog/releases)):

----
#### 0.0.7
 - 2020-07-15 - [1 commit(s)](https://github.com/shipkit/shipkit-changelog/compare/v0.0.6...v0.0.7) by Szczepan Faber
 - Fixed broken links [(#12)](https://github.com/shipkit/shipkit-changelog/pull/12)
----

Also check out [shipkit-auto-version](https://github.com/shipkit/shipkit-auto-version) plugin that automatically picks the next version for the release.
```shipkit-auto-version``` and ```shipkit-changelog``` plugins work together perfectly.

## Basic usage

```groovy
plugins {  
    id 'org.shipkit.shipkit-changelog'
}

tasks.named("generateChangelog") {
    previousRevision = "v0.0.1"
    repository = "shipkit/shipkit-changelog"
    githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
    newTagRevision = System.getenv("GIT_SHA")   // using an env var automatically exported by GH action
}

```

## Realistic example

Realistic example, also uses a sibling plugin [shipkit-auto-version](https://github.com/shipkit/shipkit-auto-version) plugin
(source: [gradle/release.gradle](https://github.com/shipkit/shipkit-changelog/blob/master/gradle/release.gradle))

```groovy
    plugins {
        id 'org.shipkit.shipkit-changelog'
        id 'org.shipkit.shipkit-gh-release'
        id 'org.shipkit.shipkit-auto-version'
    }

    tasks.named("generateChangelog") {
        previousRevision = project.ext.'shipkit-auto-version.previous-tag'
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
        repository = "shipkit/shipkit-changelog"
    }
    
    tasks.named("githubRelease") {
        dependsOn tasks.named("generateChangelog")
        repository = "shipkit/shipkit-changelog"
        changelog = tasks.named("generateChangelog").get().outputFile
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
        newTagRevision = System.getenv("GIT_SHA")   // using an env var automatically exported by GH action
    }
```

## Configuration reference

### GitHub access tokens

The standard way to enable the automated tasks read/write to GitHub are the
[personal access tokens](https://docs.github.com/en/free-pro-team@latest/github/authenticating-to-github/creating-a-personal-access-token#creating-a-token).
Shipkit Changelog plugin needs a token to fetch tickets and post releases via GH REST API. 
The token is set in the task configuration in \*.gradle file via `githubToken` property.
When creating a new token using GH UI, make sure to select the `repo/public_repo` *scope*
([more info on scopes](https://docs.github.com/en/free-pro-team@latest/developers/apps/scopes-for-oauth-apps)).

When using GH Actions then you can use the built-in `GITHUB_TOKEN` secret.
You can pass it via an environment variable:

```yaml
    - name: Publish githubRelease
      run: ./gradlew githubRelease
      env:
        GITHUB_TOKEN: ${{secrets.GITHUB_TOKEN}}
```

Read more about GH Action's [GITHUB_TOKEN](https://docs.github.com/en/free-pro-team@latest/actions/reference/authentication-in-a-workflow).
In Shipkit Changelog plugin the `githubToken` property should be supplied by the env variable to keep things safe.
The token grants write access to the repository and it ***should not*** be exposed / checked-in.

### Fetch depth on CI

CI systems are often configured by default to perform Git fetch with minimum amount of commits.
However, our changelog plugin needs commits in order to generate the release notes.
When using GH actions, please configure your checkout action to fetch the entire history.
Based on our tests in Mockito project, the checkout of the *entire* Mockito history (dating 2008)
has negligible performance implication (adds ~2 secs to the checkout).

```yaml
- uses: actions/checkout@v2   # docs: https://github.com/actions/checkout
  with:
    fetch-depth: '0' # will fetch the entire history
```

### Target revision

For proper release tagging the `newTagRevision` property needs to be set.
This property is set with the SHA of the commit that will be tagged when GH release is created.
Recommended way to do this is to use your CI system's built-in env variable that exposes the revision the CI is building.
GH actions expose `GITHUB_SHA` and hence we are using it in the code samples.
You can read more about all [default env variables](https://docs.github.com/en/free-pro-team@latest/actions/reference/environment-variables#default-environment-variables) exposed by GH actions.
Note that you can use *any* CI system, not necessarily GH actions.
Just refer to the documentation of your CI system to learn what are its default env variables.

## Customers / sample projects

- https://github.com/shipkit/shipkit-demo (great example/reference project)
- https://github.com/shipkit/shipkit-changelog (this project)
- https://github.com/shipkit/shipkit-auto-version
- https://github.com/mockito/mockito
- https://github.com/mockito/mockito-scala
- https://github.com/mockito/mockito-testng

## Other plugins/tools

There are other Gradle plugins or tools that provide similar functionality:

1. [github-changelog-generator](https://github.com/github-changelog-generator/github-changelog-generator)
is a popular Ruby Gem (6K stars on GitHub) that generates changelog based on commits/pull requests
but does not publish GitHub releases ([#56](https://github.com/github-changelog-generator/github-changelog-generator/issues/56)).
Our plugin is a pure Gradle solution and it can publish a GitHub release.

2. GitHub Action [Release Drafter](https://github.com/marketplace/actions/release-drafter)
drafts the next release notes as pull requests are merged.
This is a good option when the team wants to release on demand. 
Our plugin is great for fully automated releases on every merged pull request.

3. Gradle Plugin [git-changelog-gradle-plugin](https://github.com/tomasbjerre/git-changelog-gradle-plugin)
seems like a nice plugin, maintained but not very popular (<50 stars on GitHub) and pulls in a lot of other dependencies
([#21](https://github.com/tomasbjerre/git-changelog-gradle-plugin/issues/21)).
Our plugin is simpler, smaller and brings only one dependency (that is very small, simple and has no transitive dependencies).

4. Semantic Release [semantic-release](https://github.com/semantic-release/semantic-release)
is a npm module for fully automated "semantic" releases, with changelog generation.  
It has impressive 10K stars on GitHub.
Our plugin is less opinionated, smaller, simpler and a pure Gradle solution.

Pick the best tool that work for you and start automating releases and changelog generation!

## Design
     
### Changelog generation

1. Collect commits between 2 revisions
2. Find ticket IDs based on '#' prefix in commit messages (e.g. looking for #1, #50, etc.)
3. Use GitHub REST API to collect ticket information (issue or pull request) from GitHub
4. Create markdown file using the PR/issue titles 

### Posting GitHub releases

Uses GitHub REST API to post releases. 

## Usage

### 'org.shipkit.shipkit-changelog' plugin

Basic task configuration
(source: [ChangelogPluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/changelog/ChangelogPluginIntegTest.groovy))

```groovy
    plugins {  
        id 'org.shipkit.shipkit-changelog'
    }
    
    tasks.named("generateChangelog") {
        previousRevision = "v3.3.10"
        repository = "mockito/mockito"
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
        newTagRevision = System.getenv("GIT_SHA")   // using an env var automatically exported by GH action
    }
```

Complete task configuration
(source: [ChangelogPluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/changelog/ChangelogPluginIntegTest.groovy))

```groovy
    plugins {  
        id 'org.shipkit.shipkit-changelog'
    }
    
    tasks.named("generateChangelog") {
        //file where the release notes are generated, default as below
        outputFile = new File(buildDir, "changelog.md")
        
        //Working directory for running 'git' commands, default as below
        workingDir = project.projectDir                
        
        //GitHub url, configure if you use GitHub Enterprise, default as below
        ghUrl = "https://github.com"
        
        //GitHub API url, configure if you use GitHub Enterprise, default as below
        ghApiUrl = "https://api.github.com"
        
        //The release date, the default is today date 
        date = "2020-06-06"
        
        //Previous revision to generate changelog, *no default*
        previousRevision = "v3.3.10"
        
        //Current revision to generate changelog, default as below 
        revision = "HEAD" 
        
        //The release version, default as below
        version = project.version             
        
        //Repository to look for tickets, *no default*
        repository = "mockito/mockito"
        
        //Token used for fetching tickets, *no default*
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets 
    }              
```

### 'org.shipkit.shipkit-gh-release'

Basic task configuration
(source: [GitHubReleasePluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/gh/release/GitHubReleasePluginIntegTest.groovy))

```groovy
    plugins {
        id 'org.shipkit.shipkit-gh-release'
    }
                
    tasks.named("githubRelease") {
        repository = "shipkit/shipkit-changelog"
        changelog = file("changelog.md")
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
        newTagRevision = System.getenv("GIT_SHA")   // using an env var automatically exported by GH action        
    }
```

Complete task configuration
(source: [GitHubReleasePluginIntegTest](https://github.com/shipkit/shipkit-changelog/blob/master/src/integTest/groovy/org/shipkit/gh/release/GitHubReleasePluginIntegTest.groovy))

```groovy
    plugins {
        id 'org.shipkit.shipkit-gh-release'
    }
                
    tasks.named("githubRelease") {
        //GitHub API url, configure if you use GitHub Enterprise, default as below
        ghApiUrl = "https://api.github.com"
        
        //Repository where to create a release, *no default*
        repository = "shipkit/shipkit-changelog"
        
        //The file with changelog (release notes), *no default*
        changelog = file("changelog.md")
        
        //The name of the release name, default as below
        releaseName = "v" + project.version
        
        //The release tag, default as below
        releaseName = "v" + project.version      

        //Github token used for posting to GH API, *no default*
        githubToken = System.getenv("GITHUB_TOKEN") // using env var to avoid checked-in secrets
        
        //SHA of the revision from which release is created; *no default*
        newTagRevision = System.getenv("GIT_SHA")   // using an env var automatically exported by GH action        
    }
``` 

# Contributing

This project loves contributions!

## Testing

In order to test the plugin behavior locally, you need to first *install* the plugin locally,
and then use the *locally released* version in a selected *test project*.
Example workflow:

1. Clone this repo, load the project into the IntelliJ IDEA, and make the code changes.
2. Run ```./gradlew publishToMavenLocal``` task to publish the new version locally.
This version contains *your* changes implemented in the *previous* step.
Observe the build output and note down the *version* that was published.
We are using *maven local* because that's the easiest way to publish and consume a new version locally.
3. Select a test project where you want to observe/test changes implemented in the previous step.
For example, you can use *your fork* of this repo as the test project.
4. Ensure that the test project is correctly *configured* in the Gradle build file.
It needs to declare ```mavenLocal()``` as a *first* repository in ```buildscript.repositories```
([code link](https://github.com/shipkit/shipkit-changelog/blob/master/build.gradle#L3), might be stale).
Also, it needs to use the **correct** version of the plugin (the version that was published in the *earlier* step).
Here's where the version is declared in the build file: [code link](https://github.com/shipkit/shipkit-changelog/blob/master/build.gradle#L8)
(might be stale).
