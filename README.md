# zorkClone [![Build Status](https://travis-ci.org/vatbub/zorkClone.svg?branch=master)](https://travis-ci.org/vatbub/zorkClone)
This project aims to reimplement the good old nerdy game [Zork](https://en.wikipedia.org/wiki/Zork), parts I, II and III in java. 

*This is not a wrapper*, it's a reimplementation with
- A nice command window (no need for any emulator for a 1980s command line)
- Autocomplete
- A list of commands that you can execute in the current game situation

**Note:** This project has just started so nothing works right now and no downloads are offered at this stage.

## Download
As this project just started, we do not offer any official downloads yet. If you want to try a cutting edge version of the code (still very buggy), you can do the following:
  1. Download the FOKLauncher ([GitHubRepo](https://github.com/vatbub/fokLauncher)|[Download](https://bintray.com/vatbub/fokprojectsReleases/foklauncher#downloads))
  2. Download [those two files](https://github.com/vatbub/zorkClone/tree/master/foklauncher_info).
  3. Open the FOKLauncher and drag the two files into the FOKLauncher-Window
  4. Make sure to check the "Enable Snapshot Builds"-Checkbox or else you will get plenty of errors
  5. Select Zork (the game) or the game editor and enjoy using a completely buggy software :)

While you are waiting for a official release, you might want to try the [original version](https://www.infocom-if.org/downloads/downloads.html).

## Compile
If you wish to compile the project, just clone the repo and run `mvn package` ([Maven](http://maven.apache.org/) required)

## I am a nerd, I want to contribute
I am glad to know that I am not the only nerd around here so let me know via [eMail](mailto:vatbub123@googlemail.com) and I'll happily add you to the team.
Feel free to [file issues](https://github.com/vatbub/zorkClone/issues/new), fork or submit PRs.

## The roadmap ![progress](https://img.shields.io/badge/overall_progress-20%25-orange.svg)
- create a functioning framework for textbased adventure games (data model, gui, parser) ![progress](https://img.shields.io/badge/progress-40%25-yellow.svg)
- Rewrite the story of Zork I, II and III using the framework ![progress](https://img.shields.io/badge/progress-0%25-red.svg)


##Docs
[Maven Site](http://vatbubmvnsites.s3-website-us-west-2.amazonaws.com/zorkClone/1.1-SNAPSHOT/site/zorkClone/), [JavaDoc](http://vatbubmvnsites.s3-website-us-west-2.amazonaws.com/zorkClone/1.1-SNAPSHOT/site/zorkClone/apidocs/index.html)

## Contributing
Contributions of any kind are very welcome. Just fork and submit a Pull Request and we will be happy to merge. Just keep in mind that we use [Issue driven development](https://github.com/vatbub/defaultRepo/wiki/Issue-driven-development).
