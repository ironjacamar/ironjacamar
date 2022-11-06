# Contributing to IronJacamar

Welcome to the IronJacamar project! We welcome contributions from the community. This guide will walk you through the steps for getting started on our project.

- [Forking the Project](#forking-the-project)
- [Issues](#issues)
    - [Good First Issues](#good-first-issues)
- [Setting up your Developer Environment](#setting-up-your-developer-environment)
- [Contributing Guidelines](#contributing-guidelines)
- [Community](#community)


## Forking the Project
To contribute, you will first need to fork the [iron-jacamar](https://github.com/ironjacamar/ironjacamar) repository.

This can be done by looking in the top-right corner of the repository page and clicking "Fork".
![fork](assets/images/fork.jpg)

The next step is to clone your newly forked repository onto your local workspace. This can be done by going to your newly forked repository, which should be at `https://github.com/USERNAME/ironjacamar`.

Then, there will be a green button that says "Code". Click on that and copy the URL.

![clone](assets/images/clone.png)

Then, in your terminal, paste the following command:
```bash
git clone [URL]
```
Be sure to replace [URL] with the URL that you copied.

Now you have the repository on your computer!

## Issues
The IronJacamar project uses JIRA to manage issues. All issues can be found [here](https://issues.redhat.com/projects/JBJCA/issues).

To create a new issue, comment on an existing issue, or assign an issue to yourself, you'll need to first [create a JIRA account](https://issues.redhat.com/).


### Good First Issues
Want to contribute to the IronJacamar project but aren't quite sure where to start? Check out our issues with the `good-first-issue` label. These are a triaged set of issues that are great for getting started on our project. These can be found [here](https://issues.redhat.com/issues/?filter=12403030).

Once you have selected an issue you'd like to work on, make sure it's not already assigned to someone else. To assign an issue to yourself, simply click on "Start Progress". This will automatically assign the issue to you.

![jira](assets/images/jira_start_progress.png)

It is recommended that you use a separate branch for every issue you work on. To keep things straightforward and memorable, you can name each branch using the JIRA issue number. This way, you can have multiple PRs open for different issues. For example, if you were working on [JBJCA-1455](https://issues.redhat.com/browse/JBJCA-1455), you could use JBJCA-1455 as your branch name.

## Setting up your Developer Environment
You will need:

* JDK
* Git
* Maven
* An [IDE](https://en.wikipedia.org/wiki/Comparison_of_integrated_development_environments#Java)
  (e.g., [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), [Eclipse](https://www.eclipse.org/downloads/), etc.)


First `cd` to the directory where you cloned the project (eg: `cd ironjacamar`)

Add a remote ref to upstream, for pulling future updates.
For example:

```
git remote add upstream https://github.com/ironjacamar/ironjacamar
```
To build `ironjacamar` run:
```bash
mvn clean install
```

To skip the tests, use:

```bash
mvn clean install -DskipTests=true
```

To run only a specific test, use:

```bash
mvn clean install -Dtest=TestClassName
```

## Contributing Guidelines

When submitting a PR, please keep the following guidelines in mind:

1. In general, it's good practice to squash all of your commits into a single commit. For larger changes, it's ok to have multiple meaningful commits. If you need help with squashing your commits, feel free to ask us how to do this on your pull request. We're more than happy to help!

2. Please include the JIRA issue you worked on in the title of your pull request and in your commit message. For example, for [JBJCA-1455](https://issues.redhat.com/browse/JBJCA-1455), the PR title and commit message should be `[JBJCA-1455] Upgrade jakarta.transaction-api from 2.0.0 to 2.0.1`.

3. Please include the link to the JIRA issue you worked on in the description of the pull request. For example, if your PR adds a fix for [JBJCA-1455](https://issues.redhat.com/browse/JBJCA-1455), the PR description should contain a link to https://issues.redhat.com/browse/JBJCA-1455.
