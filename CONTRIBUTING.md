# How to contribute

## Found a :bug: bug or have a feature request?
When you find a bug we'd like to know about it, and currently we accept feature requests, but before you submit one via [Github Issues](https://github.com/kittinunf/Fuel/issues), please make sure that:
- It is not already reported under [issues](https://github.com/kittinunf/Fuel/issues),
- If it's related to handling the HTTP requests or responses (caching, encoding, responses), you have looked up the [HTTP/1.1 RFC](https://tools.ietf.org/html/rfc2616), or when it's HTTP/2.0 related, the [draft or released document](https://datatracker.ietf.org/wg/httpbis/documents/).
- If it's a dependency issue such as an issue with livedata or gson, you make sure it related to the fuel integration.

If you're unable to find an open issue addressing the problem, or any issue (both closed or open) about the feature request, you may open a new one. Be sure to include a title and clear description, including as much relevant information as possible, and when submitting a bug report, preferably a code sample or an executable test case demonstrating the expected behavior that is not occurring.

## Submitting changes (patch, fix, new feature implementation)

Please send a GitHub Pull Request to [Fuel](https://github.com/Fuel) with a clear list of what you have done (read more about [pull requests](https://help.github.com/articles/about-pull-requests/)).

### Pull Request
In your Pull Request be sure to include a title and clear description on what it adds, removes or changes. The guidelines for commit messages below can be applied to the Pull Request as a whole.

### Commit Messages
Always write a clear log message for your commits. Thoughtbot has written a great [article](https://robots.thoughtbot.com/5-useful-tips-for-a-better-commit-message) on better commit messages, and the following is taken and adapted from that article:

Other developers, especially you-in-two-weeks and you-from-next-year, will thank you for your forethought and verbosity when they run git blame to see why that conditional is there.

The first line should always be 50 characters or less and that it should be followed by a blank line. Many IDEs and editors ship with syntax, indent, and filetype plugins for Git commits which can help here.

Answer the following questions:

- **Why is this change necessary?**

  This question tells reviewers of your pull request what to expect in the commit, allowing them to more easily identify and point out unrelated changes.

- **How does it address the issue?**

  Describe, at a high level, what was done to affect change. "Introduce a red/black tree to increase search speed" or "Remove <troublesome dependency X>, which was causing <specific description of issue introduced by dependency>" are good examples. If your change is obvious, you may be able to omit addressing this question.

- **What side effects does this change have?**

  This is the most important question to answer, as it can point out problems where you are making too many changes in one commit or branch. One or two bullet points for related changes may be okay, but five or six are likely indicators of a commit that is doing too many things.

## Linter

Fuel uses [ktlint](https://github.com/shyiko/ktlint) to help eliminate style error. This ensures that our project has consistency in styling and/or formatting.
To make sure that you have a quick feedback loop on styling error consider install `ktlint` locally and integrate it as a github commit hook.

### Installation

``` Bash
curl -sSLO https://github.com/shyiko/ktlint/releases/download/0.29.0/ktlint &&
  chmod a+x ktlint &&
  sudo mv ktlint /usr/local/bin/
```

### Github commit hook
``` Bash
ktlint --install-git-pre-commit-hook
```

### .editorconfig
Alternatively, if you prefer to format the code in your editor, you can use the `.editorconfig` file provided by [`ktlint`](https://github.com/shyiko/ktlint/blob/master/.editorconfig). `.editorconfig` works well with many editors ([read more](https://editorconfig.org/)).

You can grab a copy of the `.editorconfig` by running the following command in the project root.
```
$ curl -O https://raw.githubusercontent.com/shyiko/ktlint/master/.editorconfig
```

### Read more about ktlint
For more detailed instruction, please visit [ktlint](https://github.com/shyiko/ktlint) repository.


