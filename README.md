# Roo Angular #

### Overview ###

There is currently no Roo plugin to generate an Angular SPA so this is an attempt to do that. 

Spring do have an outstanding request so it may happen: https://jira.spring.io/browse/ROO-3459

This plugin uses Bootstrap for the CSS and Sass to generated that CSS. It uses a number of Angular addons which are installed using Bower

## Prerequiste ##

Install web mvc and json

1. web mvc setup
2. web mvc all --package ~.web
3. json all
4. web mvc json all

## Setup ##

After you have put the plugin in the bundles folder simply run


- web anglar
- bower install (in the src\main\webapp folder)


## To do ##

This is still a work in progress, still need lots including the following

- Handle validation error
- List
- Edit