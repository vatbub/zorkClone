#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_BRANCH" = "master" ]
then
  mvn deploy --settings target/travis/settings.xml -DskipTests=true

  git checkout master
  \curl -sSL https://get.rvm.io | bash -s stable --ruby
  rvm reload
  gem install github_changelog_generator -v 1.13.0
  github_changelog_generator
  grep "." github_deploy_key >> ~/.ssh/id_rsa
  chmod 400 ~/.ssh/id_rsa
  git config --global user.email $GH_USER_EMAIL
  git config --global user.name $GH_USER_NAME
  git add CHANGELOG.md
  git commit -m "[skip ci] Updated Changelog"
  git push git@github.com:vatbub/zorkClone.git master
fi
