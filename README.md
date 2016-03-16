# WeeboBot
Weebobot is an open source chat moderation bot for [Twitch](http://twitch.tv) written in Java

Most of the core functionality focuses around the [PIrcBot API](http://www.jibble.org/pircbot.php)
which allows us to communicate with Twitch IRC

# Contributing
To make contributions to the project follow the instructions below.

---

Cloning

    git clone https://github.com/KSP-SpaceDock/SpaceDock.git

Updating

    git fetch --all
    git merge

Contributing
First you will need to fork "SpaceDock" on github, and set up your SSH key.

    git remote rm origin
    git remote add upstream https://github.com/KSP-SpaceDock/SpaceDock.git
    git remote add origin git@github.com:YOUR_USER_NAME_HERE/SpaceDock.git
    git fetch --all
    git push --set-upstream origin master

Make a branch so you don't bork your master.

    git branch bugfix-number
    git checkout bugfix-number

Do your changes here with your favourite text editor or IDE.

    git add -A
    git commit -a -m "A small message about your commit"
    git push --set-upstream origin bugfix-number

When you are happy with the code, open a pull request on github. After it is merged you can delete it and merge it in your master

    git checkout master
    git fetch --all
    git merge upstream/master
    git branch -D bugfix-number
    git push origin :bugfix-number
    
# Running the bot

If you would like to run the bot you will have to compile it yourself
by cloning the project as shown above and compiling it with `mvn install` or
downloading the latest build from [Jenkins](weebobot.com/jenkins)

